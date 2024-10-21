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
package io.meeds.kudos.service.utils;

import static org.exoplatform.social.core.manager.ActivityManagerImpl.REMOVABLE;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.processor.I18NActivityUtils;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.utils.MentionUtils;

import io.meeds.kudos.entity.KudosEntity;
import io.meeds.kudos.model.GlobalSettings;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;

public class Utils {
  private static final Log                   LOG                                   = ExoLogger.getLogger(Utils.class);

  public static final String                 KUDOS_ICON                            =
                                                        "<i class='uiIcon fa fa-award uiIconKudos uiIconBlue'></i>";

  public static final String                 SCOPE_NAME                            = "ADDONS_KUDOS";

  public static final String                 SETTINGS_KEY_NAME                     = "ADDONS_KUDOS_SETTINGS";

  public static final Context                KUDOS_CONTEXT                         = Context.GLOBAL;

  public static final Scope                  KUDOS_SCOPE                           = Scope.APPLICATION.id(SCOPE_NAME);

  public static final String                 SPACE_ACCOUNT_TYPE                    = SpaceIdentityProvider.NAME;

  public static final String                 USER_ACCOUNT_TYPE                     = "user";

  public static final String                 DEFAULT_ACCESS_PERMISSION             = "defaultAccessPermission";

  public static final String                 DEFAULT_KUDOS_PER_PERIOD              = "defaultKudosPerPeriod";

  public static final String                 KUDOS_RECEIVER_NOTIFICATION_ID        = "KudosActivityReceiverNotificationPlugin";

  public static final String                 KUDOS_SENT_EVENT                      = "exo.kudos.sent";

  public static final String                 KUDOS_ACTIVITY_EVENT                  = "exo.kudos.activity";

  public static final String                 GAMIFICATION_GENERIC_EVENT            = "exo.gamification.generic.action";

  public static final String                 KUDOS_CANCEL_ACTIVITY_EVENT           = "kudos.cancel.activity";

  public static final String                 GAMIFICATION_CANCEL_EVENT             = "gamification.cancel.event.action";

  public static final String                 GAMIFICATION_RECEIVE_KUDOS_EVENT_NAME = "receiveKudos";

  public static final String                 GAMIFICATION_SEND_KUDOS_EVENT_NAME    = "sendKudos";

  public static final String                 GAMIFICATION_OBJECT_TYPE              = "activity";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TYPE           = "exokudos:activity";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TITLE_ID       = "activity_kudos_content";

  public static final String                 KUDOS_MESSAGE_PARAM                   = "kudosMessage";

  public static final ArgumentLiteral<Kudos> KUDOS_DETAILS_PARAMETER               = new ArgumentLiteral<>(Kudos.class, "kudos");

  public static final String                 ACTIVITY_COMMENT_ID_PREFIX            = "comment";

  public static final String                 CONTENT_TYPE                          = "contentType";

  private Utils() {
  }

  public static Space getSpace(String id) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (id.indexOf(SpaceUtils.SPACE_GROUP) >= 0) {
      return spaceService.getSpaceByGroupId(id);
    }
    Space space = spaceService.getSpaceById(id);
    if (space == null) {
      space = spaceService.getSpaceByPrettyName(id);
      if (space == null) {
        space = spaceService.getSpaceByGroupId("/spaces/" + id);
      }
    }
    return space;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }

  public static List<String> getNotificationReceiversUsers(String receiverType, String receiverId, String senderId) {
    if (SPACE_ACCOUNT_TYPE.equals(receiverType)) {
      Space space = getSpace(receiverId);
      if (space == null) {
        return Collections.singletonList(receiverId);
      } else {
        String[] members = space.getMembers();
        if (members == null || members.length == 0) {
          return Collections.emptyList();
        } else if (StringUtils.isBlank(senderId)) {
          return Arrays.asList(members);
        } else {
          return Arrays.stream(members).filter(member -> !senderId.equals(member)).toList();
        }
      }
    } else {
      return Collections.singletonList(receiverId);
    }
  }

  public static String getReceiverIdentityProviderType(String receiverType) {
    return SPACE_ACCOUNT_TYPE.equals(receiverType) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
  }

  public static String getReceiverType(String receiverType) {
    return SPACE_ACCOUNT_TYPE.equals(receiverType) ? SPACE_ACCOUNT_TYPE : USER_ACCOUNT_TYPE;
  }

  public static Kudos fromEntity(KudosEntity kudosEntity) {
    if (kudosEntity == null) {
      return null;
    }
    Kudos kudos = new Kudos();
    kudos.setTechnicalId(kudosEntity.getId());
    kudos.setMessage(kudosEntity.getMessage());
    kudos.setEntityId(String.valueOf(kudosEntity.getEntityId()));
    kudos.setActivityId(kudosEntity.getActivityId());
    if (kudosEntity.getParentEntityId() != null && kudosEntity.getParentEntityId() != 0) {
      kudos.setParentEntityId(String.valueOf(kudosEntity.getParentEntityId()));
    }
    kudos.setEntityType(KudosEntityType.values()[kudosEntity.getEntityType()].name());
    kudos.setTimeInSeconds(kudosEntity.getCreatedDate());

    if (kudosEntity.isReceiverUser()) {
      Identity receiverIdentity = getIdentityById(kudosEntity.getReceiverId());
      kudos.setReceiverId(receiverIdentity.getRemoteId());
      kudos.setReceiverIdentityId(getIdentityIdByType(receiverIdentity));
      kudos.setReceiverType(USER_ACCOUNT_TYPE);
      kudos.setReceiverPosition(StringEscapeUtils.unescapeHtml4(receiverIdentity.getProfile().getPosition()));
      kudos.setExternalReceiver(receiverIdentity.getProfile() != null
          && receiverIdentity.getProfile().getProperty("external") != null
          && receiverIdentity.getProfile().getProperty("external").equals("true"));
      kudos.setEnabledReceiver(receiverIdentity.isEnable() && !receiverIdentity.isDeleted());
      kudos.setReceiverFullName(receiverIdentity.getProfile().getFullName());
      kudos.setReceiverURL(LinkProvider.getUserProfileUri(receiverIdentity.getRemoteId()));
      kudos.setReceiverAvatar(getAvatar(receiverIdentity, null));
    } else {
      Space space = getSpace(String.valueOf(kudosEntity.getReceiverId()));
      if (space != null) {
        kudos.setReceiverId(space.getPrettyName());
        kudos.setReceiverIdentityId(String.valueOf(kudosEntity.getReceiverId()));
        kudos.setReceiverType(SPACE_ACCOUNT_TYPE);
        kudos.setReceiverFullName(space.getDisplayName());
        kudos.setReceiverURL(LinkProvider.getActivityUriForSpace(space.getPrettyName(),
                                                                 space.getGroupId().replace("/spaces/", "")));
        kudos.setReceiverAvatar(getAvatar(null, space));
      }
    }

    Identity senderIdentity = getIdentityById(kudosEntity.getSenderId());
    kudos.setSenderId(senderIdentity.getRemoteId());
    kudos.setSenderIdentityId(getIdentityIdByType(senderIdentity));
    kudos.setSenderFullName(senderIdentity.getProfile().getFullName());
    kudos.setSenderURL(LinkProvider.getUserProfileUri(senderIdentity.getRemoteId()));
    kudos.setSenderAvatar(getAvatar(senderIdentity, null));
    return kudos;
  }

  public static KudosEntity toEntity(Kudos kudos) {
    KudosEntity kudosEntity = new KudosEntity();
    kudosEntity.setId(kudos.getTechnicalId());
    kudosEntity.setMessage(kudos.getMessage());
    kudosEntity.setEntityId(Long.parseLong(kudos.getEntityId()));
    kudosEntity.setActivityId(kudos.getActivityId());
    if (StringUtils.isNoneBlank(kudos.getParentEntityId())) {
      kudosEntity.setParentEntityId(Long.parseLong(kudos.getParentEntityId()));
    }
    kudosEntity.setEntityType(KudosEntityType.valueOf(kudos.getEntityType()).ordinal());
    kudosEntity.setSenderId(Long.parseLong(kudos.getSenderIdentityId()));

    boolean isReceiverUser = OrganizationIdentityProvider.NAME.equals(kudos.getReceiverType())
        || USER_ACCOUNT_TYPE.equals(kudos.getReceiverType());
    kudosEntity.setReceiverUser(isReceiverUser);
    kudosEntity.setReceiverId(Long.parseLong(kudos.getReceiverIdentityId()));
    kudosEntity.setCreatedDate(kudos.getTimeInSeconds());
    return kudosEntity;
  }

  public static LocalDateTime timeFromSeconds(long dateInSeconds) {
    if (dateInSeconds <= 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(dateInSeconds), TimeZone.getDefault().toZoneId());
  }

  public static long timeToSeconds(LocalDateTime time) {
    return time.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  public static KudosPeriod getCurrentPeriod(GlobalSettings globalSettings) {
    return getPeriodOfTime(globalSettings, LocalDateTime.now());
  }

  public static Identity getIdentityByTypeAndId(String type, String name) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getOrCreateIdentity(type, name);
  }

  public static KudosPeriod getPeriodOfTime(GlobalSettings globalSettings, LocalDateTime localDateTime) {
    KudosPeriodType kudosPeriodType = getPeriodType(globalSettings);
    return kudosPeriodType.getPeriodOfTime(localDateTime);
  }

  public static KudosPeriodType getPeriodType(GlobalSettings globalSettings) {
    KudosPeriodType kudosPeriodType = null;
    if (globalSettings == null || globalSettings.getKudosPeriodType() == null) {
      LOG.warn("Provided globalSettings doesn't have a parametred kudos period type, using MONTH period type: " + globalSettings,
               new RuntimeException());
      kudosPeriodType = KudosPeriodType.DEFAULT;
    } else {
      kudosPeriodType = globalSettings.getKudosPeriodType();
    }
    return kudosPeriodType;
  }

  public static void computeKudosActivityProperties(ExoSocialActivity activity, Kudos kudos) {
    String senderLink = "<a href='" + kudos.getSenderURL() + "'>" + kudos.getSenderFullName() + "</a>";
    senderLink = StringEscapeUtils.unescapeHtml4(senderLink);
    String receiverLink = "<a href='" + kudos.getReceiverURL() + "'>" + kudos.getReceiverFullName() + "</a>";
    receiverLink = StringEscapeUtils.unescapeHtml4(receiverLink);

    String kudosMessage = MentionUtils.substituteUsernames(kudos.getMessage());
    String message = StringUtils.isBlank(kudosMessage) ? "." : ": " + kudosMessage;

    if (activity.getTemplateParams() != null) {
      activity.getTemplateParams().remove(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS);
      activity.getTemplateParams().remove(I18NActivityUtils.RESOURCE_BUNDLE_KEY_TO_PROCESS);
      activity.getTemplateParams().remove(I18NActivityUtils.RESOURCE_BUNDLE_VALUES_PARAM);
      activity.getTemplateParams().remove(KUDOS_MESSAGE_PARAM);
      activity.getTemplateParams().remove(CONTENT_TYPE);
      activity.getTemplateParams().remove(REMOVABLE);
    }
    activity.setTitleId(null);

    I18NActivityUtils.addResourceKeyToProcess(activity, KUDOS_ACTIVITY_COMMENT_TITLE_ID);
    I18NActivityUtils.addResourceKey(activity, KUDOS_ACTIVITY_COMMENT_TITLE_ID, senderLink, receiverLink, message, KUDOS_ICON);
    activity.getTemplateParams()
            .put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS,
                 I18NActivityUtils.RESOURCE_BUNDLE_VALUES_PARAM + BaseActivityProcessorPlugin.TEMPLATE_PARAM_LIST_DELIM
                     + KUDOS_MESSAGE_PARAM);
    activity.getTemplateParams().put(KUDOS_MESSAGE_PARAM, kudosMessage);
    activity.getTemplateParams().put(CONTENT_TYPE, KUDOS_MESSAGE_PARAM);
    activity.getTemplateParams().put(REMOVABLE, "false");
  }

  private static String getIdentityIdByType(Identity receiverIdentity) {
    if (SpaceIdentityProvider.NAME.equals(receiverIdentity.getProviderId())) {
      Space space = getSpace(receiverIdentity.getRemoteId());
      if (space != null) {
        return space.getId();
      }
    }
    return receiverIdentity.getId();
  }

  private static Identity getIdentityById(long identityId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getIdentity(String.valueOf(identityId));
  }

  private static String getAvatar(Identity identity, Space space) {
    String avatarUrl = null;
    if (identity != null && identity.getProfile() != null) {
      avatarUrl = identity.getProfile().getAvatarUrl();
      if (StringUtils.isBlank(avatarUrl)) {
        avatarUrl = "/rest/v1/social/users/" + identity.getRemoteId() + "/avatar";
      }
    } else if (space != null) {
      avatarUrl = space.getAvatarUrl();
      if (StringUtils.isBlank(avatarUrl)) {
        avatarUrl = "/rest/v1/social/spaces/" + space.getPrettyName() + "/avatar";
      }
    }
    return avatarUrl;
  }

  public static long getActivityId(String id) {
    return StringUtils.isBlank(id) ? null : Long.valueOf(id.replace(ACTIVITY_COMMENT_ID_PREFIX, ""));
  }
}
