/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.kudos.listener;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.manager.ActivityManager;

@Asynchronous
public class KudosCanceledListener extends Listener<KudosService, Kudos> {

  private PortalContainer container;

  public KudosCanceledListener(PortalContainer container) {
    this.container = container;
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    if (kudos != null && kudos.getActivityId() > 0) {
      container.getComponentInstanceOfType(ActivityManager.class).deleteActivity(String.valueOf(kudos.getActivityId()));
    }
  }

}
