/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.service;

import static io.meeds.kudos.service.utils.Utils.KUDOS_ACTIVITY_EVENT;
import static io.meeds.kudos.service.utils.Utils.KUDOS_CANCEL_ACTIVITY_EVENT;
import static io.meeds.kudos.service.utils.Utils.KUDOS_CONTEXT;
import static io.meeds.kudos.service.utils.Utils.KUDOS_SCOPE;
import static io.meeds.kudos.service.utils.Utils.KUDOS_SENT_EVENT;
import static io.meeds.kudos.service.utils.Utils.SETTINGS_KEY_NAME;
import static io.meeds.kudos.service.utils.Utils.SPACE_ACCOUNT_TYPE;
import static io.meeds.kudos.service.utils.Utils.USER_ACCOUNT_TYPE;
import static io.meeds.kudos.service.utils.Utils.getActivityId;
import static io.meeds.kudos.service.utils.Utils.getCurrentPeriod;
import static io.meeds.kudos.service.utils.Utils.getPeriodOfTime;
import static io.meeds.kudos.service.utils.Utils.getPeriodType;
import static io.meeds.kudos.service.utils.Utils.getSpace;
import static io.meeds.kudos.service.utils.Utils.timeFromSeconds;
import static io.meeds.kudos.service.utils.Utils.timeToSeconds;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rpc.RPCService;
import org.exoplatform.services.rpc.RemoteCommand;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.kudos.model.AccountSettings;
import io.meeds.kudos.model.GlobalSettings;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.model.exception.KudosAlreadyLinkedException;
import io.meeds.kudos.storage.KudosStorage;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

/**
 * A service to manage kudos
 */
@Service
public class KudosService {

  private static final Log    LOG                             = ExoLogger.getLogger(KudosService.class);

  private static final String CLUSTER_GLOBAL_SETTINGS_UPDATED = "KudosService-GlobalSettings-Updated";

  private static final String CLUSTER_NODE_ID                 = UUID.randomUUID().toString();

  @Autowired
  private ActivityManager     activityManager;

  @Autowired
  private IdentityManager     identityManager;

  @Autowired
  private ListenerService     listenerService;

  @Autowired
  private KudosStorage        kudosStorage;

  @Autowired
  private SpaceService        spaceService;

  @Autowired
  private SettingService      settingService;

  @Autowired(required = false)
  private RPCService          rpcService;

  private GlobalSettings      globalSettings;

  @Value("${kudos.defaultAccessPermission:}") // NOSONAR
  private String              defaultAccessPermission;

  @Value("${kudos.defaultKudosPerPeriod:3}")
  private long                defaultKudosPerPeriod;

  /**
   * The generic command used to replicate changes over the cluster
   */
  private RemoteCommand       reloadSettingsCommand;

  @PostConstruct
  public void init() {
    GlobalSettings loadedGlobalSettings = loadGlobalSettings();
    if (loadedGlobalSettings == null) {
      this.globalSettings = new GlobalSettings();
      this.globalSettings.setKudosPerPeriod(defaultKudosPerPeriod);
    } else {
      this.globalSettings = loadedGlobalSettings;
    }
    installClusterListener();
  }

  /**
   * @return {@link GlobalSettings} of Kudos module
   */
  public GlobalSettings getGlobalSettings() {
    if (this.globalSettings == null) {
      this.globalSettings = loadGlobalSettings();
    }
    return this.globalSettings;
  }

  /**
   * Stores new parameters of Kudos module
   * 
   * @param settings {@link GlobalSettings}
   */
  public void saveGlobalSettings(GlobalSettings settings) {
    settingService.set(KUDOS_CONTEXT, KUDOS_SCOPE, SETTINGS_KEY_NAME, SettingValue.create(settings.toStringToPersist()));
    this.globalSettings = null;
    clearCacheClusterWide();
  }

  /**
   * @param username username to get its settings
   * @return kudos settings of a user
   */
  public AccountSettings getAccountSettings(String username) {
    AccountSettings accountSettings = new AccountSettings();
    Identity senderIdentity = (Identity) checkStatusAndGetReceiver(OrganizationIdentityProvider.NAME, username);
    long senderIdentityId = Long.parseLong(senderIdentity.getId());
    long sentKudos = kudosStorage.countKudosByPeriodAndSender(getCurrentKudosPeriod(), senderIdentityId);
    accountSettings.setRemainingKudos(getAllowedKudosPerPeriod() - sentKudos);
    return accountSettings;
  }

  /**
   * Create a new Kudos sent by current user
   * 
   * @param kudos {@link Kudos} to create
   * @param currentUser username of current user
   * @return created {@link Kudos}
   * @throws IllegalAccessException when receiver or sender aren't allowed.
   */
  @SneakyThrows
  public Kudos createKudos(Kudos kudos, String currentUser) throws IllegalAccessException {
    if (!StringUtils.equals(currentUser, kudos.getSenderId())) {
      throw new IllegalAccessException("User with id '" + currentUser + "' is not authorized to send kudos on behalf of " +
          kudos.getSenderId());
    }
    if (StringUtils.equals(currentUser, kudos.getReceiverId())) {
      throw new IllegalAccessException("User with username '" + currentUser + "' is not authorized to send kudos to himseld!");
    }
    if (StringUtils.isNotBlank(kudos.getSpacePrettyName())) {
      Space space = getSpace(kudos.getSpacePrettyName());
      if (space == null) {
        throw new ObjectNotFoundException("Space not found");
      } else if (!canSendKudosInSpace(kudos, space, currentUser)) {
        throw new IllegalAccessException("User cannot redact on space");
      } else if (!isActivityComment(kudos)
                 && SPACE_ACCOUNT_TYPE.equals(kudos.getReceiverType())
                 && !isReceiverSpaceTargetAudience(kudos, space)) {
        throw new IllegalAccessException("Target space isn't the space receiving the kudos");
      }
    } else if (!isActivityComment(kudos)
               && SPACE_ACCOUNT_TYPE.equals(kudos.getReceiverType())) {
      throw new IllegalAccessException("Target space isn't the space receiving the kudos");
    }
    KudosPeriod currentPeriod = getCurrentKudosPeriod();

    Identity senderIdentity = (Identity) checkStatusAndGetReceiver(OrganizationIdentityProvider.NAME, currentUser);
    long senderIdentityId = Long.parseLong(senderIdentity.getId());
    if (kudosStorage.countKudosByPeriodAndSender(currentPeriod, senderIdentityId) >= getAllowedKudosPerPeriod()) {
      throw new IllegalAccessException("User having username'" + currentUser + "' is not authorized to send more kudos");
    }

    kudos.setSenderId(senderIdentity.getRemoteId());
    kudos.setSenderIdentityId(senderIdentity.getId());
    Object receiverObject = checkStatusAndGetReceiver(kudos.getReceiverType(), kudos.getReceiverId());

    if (kudos.getReceiverIdentityId() == null) {
      if (receiverObject instanceof Identity identity && identity.isUser()) {
        kudos.setReceiverId(identity.getRemoteId());
        kudos.setReceiverType(USER_ACCOUNT_TYPE);
        kudos.setReceiverIdentityId(identity.getId());
      } else if (receiverObject instanceof Identity identity && identity.isSpace()) {
        Space space = getSpace(identity.getRemoteId());
        kudos.setReceiverIdentityId(space.getId());
        kudos.setReceiverId(space.getPrettyName());
        kudos.setReceiverType(SPACE_ACCOUNT_TYPE);
      } else if (receiverObject instanceof Space space) {
        if (canSendKudosInSpace(kudos, space, currentUser)) {
          kudos.setReceiverId(space.getPrettyName());
          kudos.setReceiverIdentityId(space.getId());
          kudos.setReceiverType(SPACE_ACCOUNT_TYPE);
        } else {
          throw new IllegalAccessException("User cannot redact on space");
        }
      }
    }

    kudos.setTimeInSeconds(timeToSeconds(LocalDateTime.now()));
    Kudos createdKudos = kudosStorage.createKudos(kudos);
    createdKudos.setSpacePrettyName(kudos.getSpacePrettyName());

    listenerService.broadcast(KUDOS_SENT_EVENT, this, createdKudos);

    return kudosStorage.getKudoById(createdKudos.getTechnicalId());
  }

  /**
   * @param kudos {@link Kudos} to create
   * @param space target {@link Space}
   * @param username user making the action
   * @return true if can redact on space or if is a comment/reply on an existing activity
   */
  public boolean canSendKudosInSpace(Kudos kudos, Space space, String username) {
    return spaceService.canRedactOnSpace(space, username)
        || (isActivityComment(kudos) && spaceService.canViewSpace(space, username));
  }

  /**
   * Deletes a sent kudos
   *
   * @param kudosId Kudos technical identifier to delete
   * @param username User name deleting kudos
   * @throws IllegalAccessException when user is not authorized to delete the kudos
   * @throws ObjectNotFoundException when the kudos identified by its technical identifier is not found
   * @throws KudosAlreadyLinkedException when the kudos is already linked to kudos entities 
   */
  @SneakyThrows
  public void deleteKudosById(long kudosId, String username) throws IllegalAccessException, ObjectNotFoundException, KudosAlreadyLinkedException  {
    if (username == null) {
      throw new IllegalArgumentException("Username is mandatory");
    }
    if (kudosId <= 0) {
      throw new IllegalArgumentException("Kudos id has to be positive integer");
    }
    Kudos kudos = kudosStorage.getKudoById(kudosId);
    if (kudos == null) {
      throw new ObjectNotFoundException("Kudos with id " + kudosId + " wasn't found");
    }
    if (!kudos.getSenderId().equals(username)) {
      throw new IllegalAccessException("user " + username + " is not allowed to delete kudos with id " + kudosId);
    }
    long kudosOfActivityCount = kudosStorage.countKudosOfActivity(kudos.getActivityId());
    if (kudosOfActivityCount > 1) {
      throw new KudosAlreadyLinkedException("kudos with id " + kudosId + "already linked to kudos entities");
    }
    deleteKudosById(kudosId);
    listenerService.broadcast(KUDOS_CANCEL_ACTIVITY_EVENT, this, kudos);
  }

  /**
   * Deletes a sent kudos
   *
   * @param kudosId Kudos technical identifier to delete
   * @throws ObjectNotFoundException when the kudos identified by its technical
   *           identifier is not found
   */
  public void deleteKudosById(long kudosId) throws ObjectNotFoundException {
    if (kudosId <= 0) {
      throw new IllegalArgumentException("Kudos id has to be positive integer");
    }
    Kudos kudos = kudosStorage.getKudoById(kudosId);
    if (kudos == null) {
      throw new ObjectNotFoundException("Kudos with id " + kudosId + " wasn't found");
    }
    kudosStorage.deleteKudosById(kudosId);
  }

  /**
   * @param kudos {@link Kudos}
   * @return true if the associated Activity to generate is a comment or a reply
   *         to a comment, else false
   */
  public boolean isActivityComment(Kudos kudos) {
    return KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.ACTIVITY
        || KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT;
  }

  /**
   * Stores generated activity for created {@link Kudos}
   * 
   * @param kudosId {@link Kudos} technical identifier
   * @param activityId {@link ExoSocialActivity} technical identifier
   */
  public void updateKudosGeneratedActivityId(long kudosId, long activityId) {
    kudosStorage.saveKudosActivityId(kudosId, activityId);
    Kudos kudos = kudosStorage.getKudoById(kudosId);
    listenerService.broadcast(KUDOS_ACTIVITY_EVENT, this, kudos);
  }

  /**
   * Retrieves kudos by activityId
   * 
   * @param activityId {@link ExoSocialActivity} identifier
   * @param currentUser {@link org.exoplatform.services.security.Identity}
   * @return {@link Kudos}
   * @throws IllegalAccessException when user doesn't have access to kudos
   */
  public Kudos getKudosByActivityId(Long activityId,
                                    org.exoplatform.services.security.Identity currentUser) throws IllegalAccessException {
    Kudos kudos = kudosStorage.getKudosByActivityId(activityId);
    if (kudos == null) {
      return null;
    }
    if (StringUtils.equals(kudos.getSenderId(), currentUser.getUserId())
        || StringUtils.equals(kudos.getReceiverId(), currentUser.getUserId())) {
      return kudos;
    }

    ExoSocialActivity activity = activityManager.getActivity(String.valueOf(activityId));
    if (activity == null) {
      // Can't check permissions, thus return null
      return null;
    }
    if (!activityManager.isActivityViewable(activity, currentUser)) {
      throw new IllegalAccessException("User " + currentUser.getUserId() + " isn't allowed to access kudos of activity with id " +
          activityId);
    }
    return kudos;
  }

  /**
   * Retrieves kudos by activityId
   * 
   * @param activityId {@link ExoSocialActivity} identifier
   * @return {@link Kudos}
   */
  public Kudos getKudosByActivityId(Long activityId) {
    return kudosStorage.getKudosByActivityId(activityId);
  }

  /**
   * Updates a kudos
   * 
   * @param kudos {@link Kudos}
   * @return {@link Kudos}
   */
  public Kudos updateKudos(Kudos kudos) {
    return kudosStorage.updateKudos(kudos);
  }

  /**
   * Retrieves the list of kudos sent in a period of time.
   * 
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosByPeriod(long startDateInSeconds, long endDateInSeconds, int limit) {
    KudosPeriod period = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    return kudosStorage.getKudosByPeriod(period, limit);
  }

  /**
   * Retrieves the list of kudos sent in a period of time.
   * 
   * @param dateInSeconds timestamp in seconds
   * @param periodType {@link KudosPeriodType} used to compute real start and
   *          end dates of period
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosByPeriod(long dateInSeconds, KudosPeriodType periodType, int limit) {
    if (periodType == null) {
      throw new IllegalArgumentException("'periodType' is mandatory");
    }
    KudosPeriod period = periodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
    return kudosStorage.getKudosByPeriod(period, limit);
  }

  /**
   * Retrieves the list of kudos sent in a period of time. Configured Period
   * Type is retrieved from {@link GlobalSettings}
   * 
   * @param dateInSeconds timestamp in seconds to compute real start and end
   *          dates of period
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosByPeriodOfDate(long dateInSeconds, int limit) {
    KudosPeriod period = getKudosPeriodOfTime(dateInSeconds);
    return kudosStorage.getKudosByPeriod(period, limit);
  }

  /**
   * Retrieves a list of kudos sent using a dedicated entity (activity, comment,
   * profile header, tiptip...)
   * 
   * @param entityType entity type of type {@link KudosEntityType}
   * @param entityId entity technical id
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosByEntity(String entityType, String entityId, int limit) {
    return kudosStorage.getKudosByEntity(entityType, entityId, limit);
  }

  public long countKudosByEntity(String entityType, String entityId) {
    return kudosStorage.countKudosByEntity(entityType, entityId);
  }

  /**
   * Count kudos sent by an identity using a dedicated entity (activity,
   * comment, profile header, tiptip...)
   * 
   * @param entityType entity type of type {@link KudosEntityType}
   * @param entityId entity technical id
   * @param senderIdentityId {@link Identity} technical id
   * @return 0 if identity not found, else kudos count
   */
  public long countKudosByEntityAndSender(String entityType, String entityId, String senderIdentityId) {
    return kudosStorage.countKudosByEntityAndSender(entityType, entityId, senderIdentityId);
  }

  /**
   * Count kudos sent by an identity in a period of time
   * 
   * @param senderIdentityId {@link Identity} technical id
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @return 0 if identity not found, else kudos count
   */
  public long countKudosByPeriodAndSender(long senderIdentityId,
                                          long startDateInSeconds,
                                          long endDateInSeconds) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    return kudosStorage.countKudosByPeriodAndSender(kudosPeriod, senderIdentityId);
  }

  /**
   * Retrieves Kudos list by sender identity Id
   * 
   * @param senderIdentityId {@link Identity} technical id of sender
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosByPeriodAndSender(long senderIdentityId,
                                               long startDateInSeconds,
                                               long endDateInSeconds,
                                               int limit) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    return kudosStorage.getKudosByPeriodAndSender(kudosPeriod, senderIdentityId, limit);
  }

  /**
   * Count kudos received by an identity in a period of time
   * 
   * @param identityId {@link Identity} technical id
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @return 0 if identity not found, else kudos count
   */
  public long countKudosByPeriodAndReceiver(long identityId,
                                            long startDateInSeconds,
                                            long endDateInSeconds) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    Identity identity = identityManager.getIdentity(String.valueOf(identityId));
    if (identity == null) {
      return 0;
    }
    return kudosStorage.countKudosByPeriodAndReceiver(kudosPeriod, identity.getProviderId(), identity.getRemoteId());
  }

  /**
   * Count kudos received by a list of identities in a period of time
   *
   * @param identitiesId {@link Identity} List of technical id
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @return Map&lt;identityId,kudoCount&gt; the number of kudos by identity
   */
  public Map<Long, Long> countKudosByPeriodAndReceivers(List<Long> identitiesId, long startDateInSeconds, long endDateInSeconds) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    return kudosStorage.countKudosByPeriodAndReceivers(kudosPeriod, identitiesId);
  }

  /**
   * Retrieves kudos received by an identity in a period of time
   * 
   * @param identityId {@link Identity} technical id
   * @param startDateInSeconds timestamp in seconds
   * @param endDateInSeconds timestamp in seconds
   * @param limit limit of results size to retrieve
   * @return {@link List} of {@link Kudos}. An empty list will be returned if
   *         the identity is not found.
   */
  public List<Kudos> getKudosByPeriodAndReceiver(long identityId,
                                                 long startDateInSeconds,
                                                 long endDateInSeconds,
                                                 int limit) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    Identity identity = identityManager.getIdentity(String.valueOf(identityId));
    if (identity == null) {
      return Collections.emptyList();
    }
    return kudosStorage.getKudosByPeriodAndReceiver(kudosPeriod, identity.getProviderId(), identity.getRemoteId(), limit);
  }

  /**
   * Retrieves the list of kudos for a given parent entity id and with a set of
   * entity types
   * 
   * @param activityId {@link ExoSocialActivity} id
   * @param currentUser user requesting access to kudos list
   * @return {@link List} of {@link Kudos}
   * @throws IllegalAccessException when user doesn't have access to kudos
   */
  public List<Kudos> getKudosListOfActivity(String activityId,
                                            org.exoplatform.services.security.Identity currentUser) throws IllegalAccessException {
    if (currentUser == null) {
      throw new IllegalArgumentException("User is mandatory");
    }
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      return Collections.emptyList();
    }
    if (!activityManager.isActivityViewable(activity, currentUser)) {
      throw new IllegalAccessException("User " + currentUser.getUserId() + " isn't allowed to access kudos of activity with id " +
          activityId);
    }
    return getKudosListOfActivity(activityId);
  }

  /**
   * Retrieves the list of kudos for a given parent entity id and with a set of
   * entity types
   * 
   * @param activityId {@link ExoSocialActivity} id
   * @return {@link List} of {@link Kudos}
   */
  public List<Kudos> getKudosListOfActivity(String activityId) {
    return kudosStorage.getKudosListOfActivity(getActivityId(activityId));
  }

  public KudosPeriodType getDefaultKudosPeriodType() {
    return getPeriodType(getGlobalSettings());
  }

  public KudosPeriod getCurrentKudosPeriod() {
    return getCurrentPeriod(getGlobalSettings());
  }

  public KudosPeriod getKudosPeriodOfTime(long dateInSeconds) {
    return getPeriodOfTime(getGlobalSettings(), timeFromSeconds(dateInSeconds));
  }

  public KudosPeriod getKudosPeriodOfTime(KudosPeriodType periodType, long dateInSeconds) {
    return periodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
  }

  private boolean isReceiverSpaceTargetAudience(Kudos kudos, Space space) {
    String receiverId = kudos.getReceiverId();
    Space targetSpaceAudience = getSpace(receiverId);
    return targetSpaceAudience != null
        && StringUtils.equals(space.getId(), targetSpaceAudience.getId());
  }

  private Object checkStatusAndGetReceiver(String type, String id) {
    if (USER_ACCOUNT_TYPE.equals(type) || OrganizationIdentityProvider.NAME.equals(type)) {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
      if (identity == null || !identity.isEnable() || identity.isDeleted()) {
        throw new IllegalStateException("User with identity id '" + id + "' doesn't have a valid and enabled social identity");
      }
      return identity;
    } else {
      Space space = getSpace(id);
      if (space == null) {
        throw new IllegalStateException("Space with id '" + id + "' wasn't found, thus it can't receive/send kudos");
      }
      return space;
    }
  }

  private long getAllowedKudosPerPeriod() {
    GlobalSettings storedGlobalSettings = getGlobalSettings();
    return storedGlobalSettings == null ? 0 : storedGlobalSettings.getKudosPerPeriod();
  }

  private GlobalSettings loadGlobalSettings() {
    SettingValue<?> globalSettingsValue = settingService.get(KUDOS_CONTEXT, KUDOS_SCOPE, SETTINGS_KEY_NAME);
    if (globalSettingsValue == null || StringUtils.isBlank(globalSettingsValue.getValue().toString())) {
      return null;
    } else {
      return GlobalSettings.parseStringToObject(globalSettingsValue.getValue().toString());
    }
  }

  private void installClusterListener() {
    if (rpcService != null) {
      // Clear global settings in current node
      // to force reload it from store
      // if another cluster node had changed
      // the settings
      this.reloadSettingsCommand = rpcService.registerCommand(new RemoteCommand() {
        public String getId() {
          return CLUSTER_GLOBAL_SETTINGS_UPDATED;
        }

        public Serializable execute(Serializable[] args) throws Throwable {
          if (!CLUSTER_NODE_ID.equals(args[0])) {
            KudosService.this.globalSettings = null;
          }
          return true;
        }
      });
    }
  }

  private void clearCacheClusterWide() {
    if (this.reloadSettingsCommand != null) {
      try {
        rpcService.executeCommandOnAllNodes(this.reloadSettingsCommand, false, CLUSTER_NODE_ID);
      } catch (Exception e) {
        LOG.warn("An error occurred while clearing global settings cache on other nodes", e);
      }
    }
  }

}
