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
package io.meeds.kudos.listener;

import static io.meeds.kudos.service.utils.Utils.KUDOS_DETAILS_PARAMETER;
import static io.meeds.kudos.service.utils.Utils.KUDOS_RECEIVER_NOTIFICATION_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

/**
 * A listener to send notification after sending a new Kudos
 */
@Component
public class KudosSentNotificationListener extends Listener<KudosService, Kudos> {

  private static final Log LOG = ExoLogger.getLogger(KudosSentNotificationListener.class);

  @Autowired
  private ListenerService  listenerService;

  @PostConstruct
  public void init() {
    listenerService.addListener("exo.kudos.sent", this);
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    try {
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.append(KUDOS_DETAILS_PARAMETER, kudos);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(KUDOS_RECEIVER_NOTIFICATION_ID))).execute(ctx);
    } catch (Exception e) {
      LOG.warn("Error sending notification for Kudos with id " + kudos.getTechnicalId(), e);
    }
  }
}
