/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.kudos.service;

import static org.exoplatform.kudos.service.utils.Utils.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.kudos.exception.KudosAlreadyLinkedException;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.statistic.ExoKudosStatistic;
import org.exoplatform.kudos.statistic.ExoKudosStatisticService;
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

/**
 * A service to manage kudos
 */
public class KudosService implements ExoKudosStatisticService, Startable {

  private static final Log    LOG                             = ExoLogger.getLogger(KudosService.class);

  private static final String CLUSTER_GLOBAL_SETTINGS_UPDATED = "KudosService-GlobalSettings-Updated";

  private static final String CLUSTER_NODE_ID                 = UUID.randomUUID().toString();

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  private ListenerService     listenerService;

  private KudosStorage        kudosStorage;

  private SettingService      settingService;

  private PortalContainer     container;

  private RPCService          rpcService;

  private GlobalSettings      globalSettings;

  /**
   * The generic command used to replicate changes over the cluster
   */
  private RemoteCommand       reloadSettingsCommand;

  public KudosService(KudosStorage kudosStorage, // NOSONAR : Needed for
                                                 // dependency injection
                      SettingService settingService,
                      ActivityManager activityManager,
                      SpaceService spaceService,
                      IdentityManager identityManager,
                      ListenerService listenerService,
                      PortalContainer container,
                      InitParams params) {
    this.kudosStorage = kudosStorage;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
    this.spaceService = spaceService;
    this.settingService = settingService;
    this.listenerService = listenerService;
    this.container = container;

    if (params != null) {
      this.globalSettings = new GlobalSettings();
      if (params.containsKey(DEFAULT_ACCESS_PERMISSION)) {
        String defaultAccessPermission = params.getValueParam(DEFAULT_ACCESS_PERMISSION).getValue();
        globalSettings.setAccessPermission(defaultAccessPermission);
      }
      if (params.containsKey(DEFAULT_KUDOS_PER_PERIOD)) {
        String defaultKudosPerPeriod = params.getValueParam(DEFAULT_KUDOS_PER_PERIOD).getValue();
        globalSettings.setKudosPerPeriod(Long.parseLong(defaultKudosPerPeriod));
      }
    }
  }

  @Override
  public void start() {
    GlobalSettings loadedGlobalSettings = loadGlobalSettings();
    if (loadedGlobalSettings != null) {
      this.globalSettings = loadedGlobalSettings;
    }
    installClusterListener();
  }

  @Override
  public void stop() {
    // Nothing to shutdown
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

    if (!isAuthorizedOnKudosModule(username)) {
      accountSettings.setDisabled(true);
      return accountSettings;
    }

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
   * @throws Exception when receiver or sender aren't allowed.
   */
  @ExoKudosStatistic(local = true, service = "kudos", operation = "create_kudos")
  public Kudos createKudos(Kudos kudos, String currentUser) throws Exception {
    if (!StringUtils.equals(currentUser, kudos.getSenderId())) {
      throw new IllegalAccessException("User with id '" + currentUser + "' is not authorized to send kudos on behalf of "
          + kudos.getSenderId());
    }
    if (StringUtils.equals(currentUser, kudos.getReceiverId())) {
      throw new IllegalAccessException("User with username '" + currentUser + "' is not authorized to send kudos to himseld!");
    }
    KudosPeriod currentPeriod = getCurrentKudosPeriod();

    Identity senderIdentity = (Identity) checkStatusAndGetReceiver(OrganizationIdentityProvider.NAME, currentUser);
    long senderIdentityId = Long.parseLong(senderIdentity.getId());
    if (kudosStorage.countKudosByPeriodAndSender(currentPeriod, senderIdentityId) >= getAllowedKudosPerPeriod()) {
      throw new IllegalAccessException("User having username'" + currentUser + "' is not authorized to send more kudos");
    }

    if (kudos.getSenderIdentityId() == null) {
      kudos.setSenderIdentityId(senderIdentity.getId());
    }
    Object receiverObject = checkStatusAndGetReceiver(kudos.getReceiverType(), kudos.getReceiverId());

    if (kudos.getReceiverIdentityId() == null) {
      if (receiverObject instanceof Identity) {
        kudos.setReceiverIdentityId(((Identity) receiverObject).getId());
      } else if (receiverObject instanceof Space) {
        kudos.setReceiverIdentityId(((Space) receiverObject).getId());
      }
    }

    kudos.setTimeInSeconds(timeToSeconds(LocalDateTime.now()));
    Kudos createdKudos = kudosStorage.createKudos(kudos);
    createdKudos.setSpacePrettyName(kudos.getSpacePrettyName());

    listenerService.broadcast(KUDOS_SENT_EVENT, this, createdKudos);

    return createdKudos;
  }

  /**
   * Deletes a sent kudos
   *
   * @param kudosId Kudos technical identifier to delete
   * @param username User name deleting kudos
   * @throws IllegalAccessException when user is not authorized to delete the
   *           kudos
   * @throws ObjectNotFoundException when the kudos identified by its technical
   *           identifier is not found
   */
  public void deleteKudosById(long kudosId, String username) throws Exception {
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
   * Stores generated activity for created {@link Kudos}
   * 
   * @param kudosId {@link Kudos} technical identifier
   * @param activityId {@link ExoSocialActivity} technical identifier
   * @throws Exception when an error happens when broadcasting event or saving
   *           activityId of Kudos
   */
  public void updateKudosGeneratedActivityId(long kudosId, long activityId) throws Exception {
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
      throw new IllegalAccessException("User " + currentUser.getUserId() + " isn't allowed to access kudos of activity with id "
          + activityId);
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
      throw new IllegalAccessException("User " + currentUser.getUserId() + " isn't allowed to access kudos of activity with id "
          + activityId);
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

  /**
   * Check if user is authorized to send/receive Kudos
   * 
   * @param username username to check
   * @return true if authorised else return false
   */
  public boolean isAuthorizedOnKudosModule(String username) {
    if (StringUtils.isBlank(username)) {
      return false;
    }
    String accessPermission = getAccessPermission();
    if (StringUtils.isBlank(accessPermission)) {
      return true;
    }
    Space space = getSpace(accessPermission);

    // Disable kudos for users not member of the permitted space members
    return spaceService.isSuperManager(username) || (space != null && spaceService.isMember(space, username));
  }

  @Override
  public Map<String, Object> getStatisticParameters(String operation, Object result, Object... methodArgs) {
    if (result == null) {
      return null;
    }
    Map<String, Object> parameters = new HashMap<>();

    Kudos savedKudos = (Kudos) result;
    parameters.put("kudos_id", savedKudos.getTechnicalId());
    parameters.put("sender_identity_id", savedKudos.getSenderIdentityId());
    parameters.put("receiver_identity_id", savedKudos.getReceiverIdentityId());
    parameters.put("kudos_entity_type", savedKudos.getEntityType());
    parameters.put("kudos_entity_id", savedKudos.getEntityId());

    String issuer = (String) methodArgs[methodArgs.length - 1];
    if (StringUtils.isNotBlank(issuer)) {
      Identity identity = getIdentityByTypeAndId(OrganizationIdentityProvider.NAME, issuer);
      if (identity != null) {
        parameters.put("user_social_id", identity.getId());
      }
    }
    return parameters;
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

  private Object checkStatusAndGetReceiver(String type, String id) {
    if (USER_ACCOUNT_TYPE.equals(type) || OrganizationIdentityProvider.NAME.equals(type)) {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
      if (identity == null || !identity.isEnable() || identity.isDeleted()) {
        throw new IllegalStateException("User with identity id '" + id + "' doesn't have a valid and enabled social identity");
      }
      if (!isAuthorizedOnKudosModule(id)) {
        throw new IllegalStateException("User with identity id '" + id
            + "' isn't member of authorized group to send/receive kudos: "
            + getAccessPermission());
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

  private String getAccessPermission() {
    GlobalSettings storedGlobalSettings = getGlobalSettings();
    return storedGlobalSettings == null ? null : storedGlobalSettings.getAccessPermission();
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
    RPCService clusterRpcService = getRpcService();
    if (clusterRpcService != null) {
      // Clear global settings in current node
      // to force reload it from store
      // if another cluster node had changed
      // the settings
      this.reloadSettingsCommand = clusterRpcService.registerCommand(new RemoteCommand() {
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
        getRpcService().executeCommandOnAllNodes(this.reloadSettingsCommand, false, CLUSTER_NODE_ID);
      } catch (Exception e) {
        LOG.warn("An error occurred while clearing global settings cache on other nodes", e);
      }
    }
  }

  private RPCService getRpcService() {
    if (rpcService == null) {
      rpcService = container.getComponentInstanceOfType(RPCService.class);
    }
    return rpcService;
  }
}
