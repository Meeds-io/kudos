package org.exoplatform.addon.kudos.service.utils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.*;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.Util;

public class Utils {
  private static final Log                   LOG                             = ExoLogger.getLogger(Utils.class);

  public static final String                 SCOPE_NAME                      = "ADDONS_KUDOS";

  public static final String                 SETTINGS_KEY_NAME               = "ADDONS_KUDOS_SETTINGS";

  public static final Context                KUDOS_CONTEXT                   = Context.GLOBAL;

  public static final Scope                  KUDOS_SCOPE                     = Scope.APPLICATION.id(SCOPE_NAME);

  public static final String                 SPACE_ACCOUNT_TYPE              = "space";

  public static final String                 USER_ACCOUNT_TYPE               = "user";

  public static final String                 DEFAULT_ACCESS_PERMISSION       = "defaultAccessPermission";

  public static final String                 DEFAULT_KUDOS_PER_PERIOD        = "defaultKudosPerPeriod";

  public static final String                 KUDOS_RECEIVER_NOTIFICATION_ID  = "KudosActivityReceiverNotificationPlugin";

  public static final String                 KUDOS_SENT_EVENT                = "exo.addons.kudos.sent";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TYPE     = "exokudos:activity";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TITLE_ID = "activity_kudos";

  public static final ArgumentLiteral<Kudos> KUDOS_DETAILS_PARAMETER         = new ArgumentLiteral<>(Kudos.class, "kudos");

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
        if (space == null) {
          space = spaceService.getSpaceByDisplayName(id);
          if (space == null) {
            space = spaceService.getSpaceByUrl(id);
          }
        }
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
    if (SPACE_ACCOUNT_TYPE.equals(receiverType) || SpaceIdentityProvider.NAME.equals(receiverType)) {
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
          return Arrays.stream(members).filter(member -> !senderId.equals(member)).collect(Collectors.toList());
        }
      }
    } else {
      return Collections.singletonList(receiverId);
    }
  }

  public static String getReceiverIdentityProviderType(String receiverType) {
    return SpaceIdentityProvider.NAME.equals(receiverType)
        || SPACE_ACCOUNT_TYPE.equals(receiverType) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
  }

  public static String getReceiverType(String receiverType) {
    return SpaceIdentityProvider.NAME.equals(receiverType) || SPACE_ACCOUNT_TYPE.equals(receiverType) ? SPACE_ACCOUNT_TYPE
                                                                                                      : USER_ACCOUNT_TYPE;
  }

  public static Kudos fromEntity(KudosEntity kudosEntity) {
    Kudos kudos = new Kudos();
    kudos.setTechnicalId(kudosEntity.getId());
    kudos.setMessage(kudosEntity.getMessage());
    kudos.setEntityId(String.valueOf(kudosEntity.getEntityId()));
    if (kudosEntity.getParentEntityId() != null && kudosEntity.getParentEntityId() != 0) {
      kudos.setParentEntityId(String.valueOf(kudosEntity.getParentEntityId()));
    }
    kudos.setEntityType(KudosEntityType.values()[kudosEntity.getEntityType()].name());
    kudos.setTime(timeFromSeconds(kudosEntity.getCreatedDate()));

    if (kudosEntity.isReceiverUser()) {
      Identity receiverIdentity = getIdentityById(kudosEntity.getReceiverId());
      kudos.setReceiverId(receiverIdentity.getRemoteId());
      kudos.setReceiverIdentityId(getIdentityIdByType(receiverIdentity));
      kudos.setReceiverType(USER_ACCOUNT_TYPE);
      kudos.setReceiverFullName(receiverIdentity.getProfile().getFullName());
      kudos.setReceiverURL(Util.getBaseUrl() + LinkProvider.getUserProfileUri(receiverIdentity.getRemoteId()));
      kudos.setReceiverAvatar(getAvatar(receiverIdentity, null));
    } else {
      Space space = getSpace(String.valueOf(kudosEntity.getReceiverId()));
      kudos.setReceiverId(space.getPrettyName());
      kudos.setReceiverIdentityId(String.valueOf(kudosEntity.getReceiverId()));
      kudos.setReceiverType(SPACE_ACCOUNT_TYPE);
      kudos.setReceiverFullName(space.getDisplayName());
      kudos.setReceiverURL(Util.getBaseUrl()
          + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", "")));
      kudos.setReceiverAvatar(getAvatar(null, space));
    }

    Identity senderIdentity = getIdentityById(kudosEntity.getSenderId());
    kudos.setSenderId(senderIdentity.getRemoteId());
    kudos.setSenderIdentityId(getIdentityIdByType(senderIdentity));
    kudos.setSenderFullName(senderIdentity.getProfile().getFullName());
    kudos.setSenderURL(Util.getBaseUrl() + LinkProvider.getUserProfileUri(senderIdentity.getRemoteId()));
    kudos.setSenderAvatar(getAvatar(senderIdentity, null));
    return kudos;
  }

  public static KudosEntity toNewEntity(Kudos kudos) {
    KudosEntity kudosEntity = new KudosEntity();
    kudosEntity.setMessage(kudos.getMessage());
    kudosEntity.setEntityId(Long.parseLong(kudos.getEntityId()));
    if (StringUtils.isNoneBlank(kudos.getParentEntityId())) {
      kudosEntity.setParentEntityId(Long.parseLong(kudos.getParentEntityId()));
    }
    kudosEntity.setEntityType(KudosEntityType.valueOf(kudos.getEntityType()).ordinal());
    kudosEntity.setSenderId(Long.parseLong(kudos.getSenderIdentityId()));

    boolean isReceiverUser = OrganizationIdentityProvider.NAME.equals(kudos.getReceiverType())
        || USER_ACCOUNT_TYPE.equals(kudos.getReceiverType());
    kudosEntity.setReceiverUser(isReceiverUser);
    kudosEntity.setReceiverId(Long.parseLong(kudos.getReceiverIdentityId()));
    kudosEntity.setCreatedDate(timeToSeconds(kudos.getTime()));
    return kudosEntity;
  }

  public static LocalDateTime timeFromSeconds(long createdDate) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(createdDate), TimeZone.getDefault().toZoneId());
  }

  public static long timeToSeconds(LocalDateTime time) {
    return time.atZone(ZoneOffset.systemDefault()).toEpochSecond();
  }

  public static KudosPeriod getCurrentPeriod(GlobalSettings globalSettings) {
    return getPeriodOfTime(globalSettings, LocalDateTime.now());
  }

  public static KudosPeriod getPeriodOfTime(GlobalSettings globalSettings, LocalDateTime localDateTime) {
    KudosPeriodType kudosPeriodType = null;
    if (globalSettings == null || globalSettings.getKudosPeriodType() == null) {
      LOG.warn("Provided globalSettings doesn't have a parametred kudos period type, using MONTH period type: " + globalSettings,
               new RuntimeException());
      kudosPeriodType = KudosPeriodType.DEFAULT;
    } else {
      kudosPeriodType = globalSettings.getKudosPeriodType();
    }
    return kudosPeriodType.getPeriodOfTime(localDateTime);
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
    return identityManager.getIdentity(String.valueOf(identityId), true);
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
}
