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
package io.meeds.kudos.notification.plugin;

import static io.meeds.kudos.service.utils.Utils.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import io.meeds.kudos.model.Kudos;

public class KudosReceiverNotificationPlugin extends BaseNotificationPlugin {

  public KudosReceiverNotificationPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return KUDOS_RECEIVER_NOTIFICATION_ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    Kudos kudos = ctx.value(KUDOS_DETAILS_PARAMETER);
    return kudos != null && kudos.getEntityType() != null;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Kudos kudos = ctx.value(KUDOS_DETAILS_PARAMETER);

    String senderId = kudos.getSenderId();
    String receiverId = kudos.getReceiverId();
    String receiverType = kudos.getReceiverType();

    List<String> toList = getNotificationReceiversUsers(receiverType, receiverId, senderId);
    if (toList == null || toList.isEmpty()) {
      return null;
    }
    ActivityManager activityManager = ExoContainerContext.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(String.valueOf(kudos.getActivityId()));
    if (activity != null && activity.isComment()) {
      activity = activityManager.getActivity(activity.getParentId());
    }

    return NotificationInfo.instance()
                           .to(toList)
                           .setSpaceId(activity == null || StringUtils.isBlank(activity.getSpaceId()) ? 0l : Long.parseLong(activity.getSpaceId()))
                           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), String.valueOf(kudos.getActivityId()))
                           .with("ENTITY_ID", kudos.getEntityId())
                           .with("ENTITY_TYPE", kudos.getEntityType())
                           .with("SENDER_ID", senderId)
                           .with("RECEIVER_ID", receiverId)
                           .with("RECEIVER_TYPE", receiverType)
                           .with("KUDOS_ID", String.valueOf(kudos.getTechnicalId()))
                           .with("KUDOS_MESSAGE",
                                 kudos.getMessage() == null ? "" : MentionUtils.substituteUsernames(kudos.getMessage()))
                           .key(getId())
                           .end();
  }

}
