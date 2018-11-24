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
package org.exoplatform.addon.kudos.service;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.addon.kudos.model.*;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.container.xml.InitParams;
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
public class KudosService implements Startable {

  private static final Log LOG            = ExoLogger.getLogger(KudosService.class);

  private IdentityManager  identityManager;

  private SpaceService     spaceService;

  private ListenerService  listenerService;

  private KudosStorage     kudosStorage;

  private SettingService   settingService;

  private GlobalSettings   globalSettings = new GlobalSettings();

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
      if (params.containsKey(DEFAULT_ACCESS_PERMISSION)) {
        String defaultAccessPermission = params.getValueParam(DEFAULT_ACCESS_PERMISSION).getValue();
        globalSettings.setAccessPermission(defaultAccessPermission);
      }
      if (params.containsKey(DEFAULT_KUDOS_PER_MONTH)) {
        String defaultKudosPerMonth = params.getValueParam(DEFAULT_KUDOS_PER_MONTH).getValue();
        globalSettings.setKudosPerMonth(Long.parseLong(defaultKudosPerMonth));
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
  }

  /**
   * Retrieve User account details DTO
   * 
   * @param id
   * @return
   */
  public AccountDetail getAccountDetails(String type, String id) {
    if (StringUtils.isBlank(type)) {
      throw new IllegalArgumentException("type parameter is mandatory");
    }
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id parameter is mandatory");
    }
    if (USER_ACCOUNT_TYPE.equals(type)) {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
      if (identity == null || identity.getProfile() == null) {
        return null;
      }

      String avatarUrl = identity.getProfile().getAvatarUrl();
      if (StringUtils.isBlank(avatarUrl)) {
        avatarUrl = "/rest/v1/social/users/" + id + "/avatar";
      }
      return new AccountDetail(id, identity.getId(), USER_ACCOUNT_TYPE, identity.getProfile().getFullName(), avatarUrl);
    } else if (SPACE_ACCOUNT_TYPE.equals(type)) {
      Space space = getSpace(id);
      if (space == null) {
        return null;
      }

      String avatarUrl = space.getAvatarUrl();
      if (StringUtils.isBlank(avatarUrl)) {
        avatarUrl = "/rest/v1/social/spaces/" + id + "/avatar";
      }
      return new AccountDetail(id, space.getId(), SPACE_ACCOUNT_TYPE, space.getDisplayName(), avatarUrl);
    } else {
      LOG.warn("Type is not recognized: " + type);
      return null;
    }
  }

  /**
   * @param username
   * @return kudos settings of a user
   */
  public AccountSettings getAccountSettings(String username) {
    AccountSettings accountSettings = new AccountSettings();

    if (!isUserAuthorized(username)) {
      accountSettings.setDisabled(true);
      return accountSettings;
    }
    long sentKudos = countKudosByMonthAndSender(YearMonth.now(), getCurrentUserId());
    accountSettings.setRemainingKudos(globalSettings.getKudosPerMonth() - sentKudos);
    return accountSettings;
  }

  public void sendKudos(String senderId, Kudos kudos) throws Exception {
    if (!StringUtils.equals(senderId, kudos.getSenderId())) {
      throw new IllegalAccessException("User '" + senderId + "' is not authorized to send kudos on behalf of "
          + kudos.getSenderId());
    }
    if (StringUtils.equals(senderId, kudos.getReceiverId())) {
      throw new IllegalAccessException("User '" + senderId + "' is not authorized to send kudos to himseld!");
    }
    if (kudosStorage.countKudosByMonthAndSender(YearMonth.now(), senderId) >= globalSettings.getKudosPerMonth()) {
      throw new IllegalAccessException("User '" + senderId + "' is not authorized to send more kudos");
    }

    checkStatus(OrganizationIdentityProvider.NAME, senderId);
    checkStatus(kudos.getReceiverType(), kudos.getReceiverId());

    kudos.setTime(LocalDateTime.now());
    kudosStorage.createKudos(kudos);

    listenerService.broadcast(KUDOS_SENT_EVENT, this, kudos);
  }

  public List<Kudos> getAllKudosByMonth(YearMonth yearMonth) {
    return kudosStorage.getAllKudosByMonth(yearMonth);
  }

  public List<Kudos> getAllKudosByMonthAndEntityType(YearMonth yearMonth, String entityType) {
    return kudosStorage.getAllKudosByMonthAndEntityType(yearMonth, entityType);
  }

  public List<Kudos> getAllKudosByEntity(String entityType, String entityId) {
    return kudosStorage.getAllKudosByEntity(entityType, entityId);
  }

  public List<Kudos> getAllKudosByMonthAndSender(YearMonth yearMonth, String identityId) {
    List<Kudos> kudosBySender = kudosStorage.getKudosByMonthAndSender(yearMonth, identityId);
    if (kudosBySender != null) {
      Collections.sort(kudosBySender);
    }
    return kudosBySender;
  }

  public List<Kudos> getKudosByMonthAndReceiver(YearMonth yearMonth, String receiverType, String receiverId) {
    List<Kudos> kudosList = kudosStorage.getKudosByMonthAndReceiver(yearMonth, receiverType, receiverId);
    if (kudosList != null) {
      Collections.sort(kudosList);
    }
    return kudosList;
  }

  public long countKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    return kudosStorage.countKudosByMonthAndSender(yearMonth, senderId);
  }

  private void checkStatus(String type, String id) {
    if (USER_ACCOUNT_TYPE.equals(type) || OrganizationIdentityProvider.NAME.equals(type)) {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
      if (identity == null || !identity.isEnable() || identity.isDeleted()) {
        throw new IllegalStateException("User '" + id + "' doesn't have a valid and enabled social identity");
      }
      if (!isUserAuthorized(id)) {
        throw new IllegalStateException("User '" + id + "' isn't member of authorized group to send/receive kudos: "
            + globalSettings.getAccessPermission());
      }
    } else {
      Space space = getSpace(id);
      if (space == null) {
        throw new IllegalStateException("Space '" + id + "' wasn't found, thus it can't receive/send kudos");
      }
    }
  }

  private boolean isUserAuthorized(String username) {
    if (StringUtils.isBlank(username)) {
      return false;
    }
    if (globalSettings == null || StringUtils.isBlank(globalSettings.getAccessPermission())) {
      return true;
    }
    Space space = getSpace(globalSettings.getAccessPermission());

    // Disable kudos for users not member of the permitted space members
    return spaceService.isSuperManager(username) || (space != null && spaceService.isMember(space, username));
  }

  public GlobalSettings getGlobalSettings() {
    if (globalSettings == null) {
      globalSettings = loadGlobalSettings();
    }
    return globalSettings;
  }

  public void saveGlobalSettings(GlobalSettings settings) {
    settingService.set(KUDOS_CONTEXT, KUDOS_SCOPE, SETTINGS_KEY_NAME, SettingValue.create(settings.toString()));
    this.globalSettings = null;
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
