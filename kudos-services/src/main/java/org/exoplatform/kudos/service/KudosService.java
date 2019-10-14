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
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.statistic.ExoKudosStatistic;
import org.exoplatform.kudos.statistic.ExoKudosStatisticService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * A service to manage kudos
 */
public class KudosService implements ExoKudosStatisticService, Startable {
  private static final Log LOG = ExoLogger.getLogger(KudosService.class);

  private IdentityManager  identityManager;

  private SpaceService     spaceService;

  private ListenerService  listenerService;

  private KudosStorage     kudosStorage;

  private SettingService   settingService;

  private GlobalSettings   globalSettings;

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
   * @param username username to get its settings
   * @return kudos settings of a user
   */
  public AccountSettings getAccountSettings(String username) {
    AccountSettings accountSettings = new AccountSettings();

    if (!isAuthorized(username)) {
      accountSettings.setDisabled(true);
      return accountSettings;
    }
    long sentKudos = countKudosBySenderInCurrentPeriod(username);
    accountSettings.setRemainingKudos(getKudosPerPeriod() - sentKudos);
    return accountSettings;
  }

  public void saveKudosActivity(long kudosId, long activityId) throws Exception {
    kudosStorage.saveKudosActivityId(kudosId, activityId);
    Kudos kudos = kudosStorage.getKudoById(kudosId);
    listenerService.broadcast(KUDOS_ACTIVITY_EVENT, this, kudos);
  }

  @ExoKudosStatistic(local = true, service = "kudos", operation = "create_kudos")
  public Kudos sendKudos(Kudos kudos, String senderId) throws Exception {
    if (!StringUtils.equals(senderId, kudos.getSenderId())) {
      throw new IllegalAccessException("User with id '" + senderId + "' is not authorized to send kudos on behalf of "
          + kudos.getSenderId());
    }
    if (StringUtils.equals(senderId, kudos.getReceiverId())) {
      throw new IllegalAccessException("User with username '" + senderId + "' is not authorized to send kudos to himseld!");
    }
    KudosPeriod currentPeriod = getCurrentKudosPeriod();

    if (kudosStorage.countKudosByPeriodAndSender(currentPeriod, senderId) >= getKudosPerPeriod()) {
      throw new IllegalAccessException("User having username'" + senderId + "' is not authorized to send more kudos");
    }

    Identity senderIdentity = (Identity) checkStatusAndGetReceiver(OrganizationIdentityProvider.NAME, senderId);
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

    kudos.setTime(LocalDateTime.now());
    kudos = kudosStorage.createKudos(kudos);

    listenerService.broadcast(KUDOS_SENT_EVENT, this, kudos);

    return kudos;
  }

  public List<Kudos> getAllKudosByPeriod(long startDateInSeconds, long endDateInSeconds) {
    KudosPeriod period = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    return kudosStorage.getAllKudosByPeriod(period);
  }

  public List<Kudos> getAllKudosByPeriodOfDate(long dateInSeconds) {
    KudosPeriod period = getKudosPeriodOfTime(dateInSeconds);
    return kudosStorage.getAllKudosByPeriod(period);
  }

  public List<Kudos> getAllKudosByEntity(String entityType, String entityId) {
    return kudosStorage.getAllKudosByEntity(entityType, entityId);
  }

  public List<Kudos> getAllKudosByEntityTypeInCurrentPeriod(String entityType) {
    return kudosStorage.getAllKudosByPeriodAndEntityType(getCurrentKudosPeriod(), entityType);
  }

  public List<Kudos> getAllKudosBySenderInCurrentPeriod(String identityId) {
    List<Kudos> kudosBySender = kudosStorage.getKudosByPeriodAndSender(getCurrentKudosPeriod(), identityId);
    if (kudosBySender != null) {
      Collections.sort(kudosBySender);
    }
    return kudosBySender;
  }

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

  public List<Kudos> getKudosByPeriodAndReceiver(long identityId,
                                                 long startDateInSeconds,
                                                 long endDateInSeconds) {
    KudosPeriod kudosPeriod = new KudosPeriod(startDateInSeconds, endDateInSeconds);
    Identity identity = identityManager.getIdentity(String.valueOf(identityId), true);
    if (identity == null) {
      return Collections.emptyList();
    }
    return kudosStorage.getKudosByPeriodAndReceiver(kudosPeriod, identity.getProviderId(), identity.getRemoteId());
  }

  public List<Kudos> getKudosByReceiverInCurrentPeriod(String receiverType, String receiverId) {
    List<Kudos> kudosList = kudosStorage.getKudosByPeriodAndReceiver(getCurrentKudosPeriod(), receiverType, receiverId);
    if (kudosList != null) {
      Collections.sort(kudosList);
    }
    return kudosList;
  }

  public long countKudosBySenderInCurrentPeriod(String senderId) {
    return kudosStorage.countKudosByPeriodAndSender(getCurrentKudosPeriod(), senderId);
  }

  public void saveGlobalSettings(GlobalSettings settings) {
    settingService.set(KUDOS_CONTEXT, KUDOS_SCOPE, SETTINGS_KEY_NAME, SettingValue.create(settings.toStringToPersist()));
    this.globalSettings = null;
  }

  public GlobalSettings getGlobalSettings() {
    if (this.globalSettings == null) {
      this.globalSettings = loadGlobalSettings();
    }
    return this.globalSettings;
  }

  public long getKudosPerPeriod() {
    GlobalSettings storedGlobalSettings = getGlobalSettings();
    return storedGlobalSettings == null ? 0 : storedGlobalSettings.getKudosPerPeriod();
  }

  public String getAccessPermission() {
    GlobalSettings storedGlobalSettings = getGlobalSettings();
    return storedGlobalSettings == null ? null : storedGlobalSettings.getAccessPermission();
  }

  /**
   * Check if user is authorized to send/receive Kudos
   * 
   * @param username username to check
   * @return true if authorised else return false
   */
  public boolean isAuthorized(String username) {
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
      if (identity == null) {
        LOG.debug("Can't find identity with remote id: {}" + issuer);
      } else {
        parameters.put("user_social_id", identity.getId());
      }
    }
    return parameters;
  }

  private KudosPeriod getCurrentKudosPeriod() {
    return getCurrentPeriod(getGlobalSettings());
  }

  private KudosPeriod getKudosPeriodOfTime(long dateInSeconds) {
    return getPeriodOfTime(getGlobalSettings(), timeFromSeconds(dateInSeconds));
  }

  private Object checkStatusAndGetReceiver(String type, String id) {
    if (USER_ACCOUNT_TYPE.equals(type) || OrganizationIdentityProvider.NAME.equals(type)) {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
      if (identity == null || !identity.isEnable() || identity.isDeleted()) {
        throw new IllegalStateException("User '" + id + "' doesn't have a valid and enabled social identity");
      }
      if (!isAuthorized(id)) {
        throw new IllegalStateException("User '" + id + "' isn't member of authorized group to send/receive kudos: "
            + getAccessPermission());
      }
      return identity;
    } else {
      Space space = getSpace(id);
      if (space == null) {
        throw new IllegalStateException("Space '" + id + "' wasn't found, thus it can't receive/send kudos");
      }
      return space;
    }
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
