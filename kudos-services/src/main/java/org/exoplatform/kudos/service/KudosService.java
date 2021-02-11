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

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.kudos.entity.KudosEntity;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.statistic.ExoKudosStatistic;
import org.exoplatform.kudos.statistic.ExoKudosStatisticService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * A service to manage kudos
 */
public class KudosService implements ExoKudosStatisticService, Startable {

  private IdentityManager identityManager;

  private SpaceService    spaceService;

  private ListenerService listenerService;

  private KudosStorage    kudosStorage;

  private SettingService  settingService;

  private GlobalSettings  globalSettings;

  public KudosService(KudosStorage kudosStorage,
                      SettingService settingService,
                      SpaceService spaceService,
                      IdentityManager identityManager,
                      ListenerService listenerService,
                      InitParams params) {
    this.kudosStorage = kudosStorage;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.settingService = settingService;
    this.listenerService = listenerService;

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
    kudos = kudosStorage.createKudos(kudos);

    listenerService.broadcast(KUDOS_SENT_EVENT, this, kudos);

    return kudos;
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
   * @param activityId {@link ExoSocialActivity}  identifier
   * @return {@link Kudos}
   */
  public KudosEntity getKudosByActivityId(Long activityId) {
   return kudosStorage.getKudosByActivityId(activityId);
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
   * Count kudos sent by an identity using a dedicated entity (activity, comment,
   * profile header, tiptip...)
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
    Identity identity = identityManager.getIdentity(String.valueOf(identityId), true);
    if (identity == null) {
      return 0;
    }
    return kudosStorage.countKudosByPeriodAndReceiver(kudosPeriod, identity.getProviderId(), identity.getRemoteId());
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
    Identity identity = identityManager.getIdentity(String.valueOf(identityId), true);
    if (identity == null) {
      return Collections.emptyList();
    }
    return kudosStorage.getKudosByPeriodAndReceiver(kudosPeriod, identity.getProviderId(), identity.getRemoteId(), limit);
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

}
