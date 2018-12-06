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
package org.exoplatform.addon.kudos.notification.plugin;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.util.List;

import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class KudosActivityReceiverNotificationPlugin extends BaseNotificationPlugin {

  public KudosActivityReceiverNotificationPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return KUDOS_ACTIVITY_RECEIVER_NOTIFICATION_ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    Kudos kudos = ctx.value(KUDOS_ACTIVITY_DETAILS_PARAMETER);
    return kudos != null && kudos.getEntityType() != null
        && (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.ACTIVITY
            || KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT);
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Kudos kudos = ctx.value(KUDOS_ACTIVITY_DETAILS_PARAMETER);

    String senderId = kudos.getSenderId();
    String receiverId = kudos.getReceiverId();
    String receiverType = kudos.getReceiverType();

    List<String> toList = getNotificationReceiversUsers(receiverType, receiverId, senderId);
    if (toList == null || toList.isEmpty()) {
      return null;
    }

    return NotificationInfo.instance()
                           .to(toList)
                           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), kudos.getEntityId())
                           .with("ENTITY_TYPE", kudos.getEntityType())
                           .with("SENDER_ID", senderId)
                           .with("RECEIVER_ID", receiverId)
                           .with("RECEIVER_TYPE", receiverType)
                           .with("KUDOS_ID", String.valueOf(kudos.getTechnicalId()))
                           .with("KUDOS_MESSAGE", kudos.getMessage() == null ? "" : kudos.getMessage())
                           .key(getId())
                           .end();
  }
}
