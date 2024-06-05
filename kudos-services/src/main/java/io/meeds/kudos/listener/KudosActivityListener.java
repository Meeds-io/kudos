/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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

import static io.meeds.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

/**
 * A listener to propagate comment or activity modification to Kudos stored
 * message
 */
@Component
public class KudosActivityListener extends ActivityListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(KudosActivityListener.class);

  @Autowired
  private ActivityManager  activityManager;

  @Autowired
  private KudosService     kudosService;

  @PostConstruct
  public void init() {
    activityManager.addActivityEventListener(this);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    ExoSocialActivity activity = activityLifeCycleEvent.getSource();
    if (activity != null && StringUtils.equals(activity.getType(), KUDOS_ACTIVITY_COMMENT_TYPE)) {
      long activityId = io.meeds.kudos.service.utils.Utils.getActivityId(activity.getId());
      Kudos kudos = kudosService.getKudosByActivityId(activityId);
      if (kudos != null) {
        String newMessage = activity.getTitle();
        kudos.setMessage(newMessage);
        kudosService.updateKudos(kudos);

        io.meeds.kudos.service.utils.Utils.computeKudosActivityProperties(activity, kudos);
        this.activityManager.updateActivity(activity, false);
      }
    }
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    ExoSocialActivity activity = activityLifeCycleEvent.getSource();
    List<Kudos> linkedKudosList = kudosService.getKudosListOfActivity(activity.getId());
    if (!linkedKudosList.isEmpty()) {
      deleteLinkedKudos(linkedKudosList);
    }
  }

  @Override
  public void deleteComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // Same as activity processing
    deleteActivity(activityLifeCycleEvent);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    updateActivity(activityLifeCycleEvent);
  }

  private void deleteLinkedKudos(List<Kudos> linkedKudosList) {
    linkedKudosList.forEach(kudos -> {
      try {
        kudosService.deleteKudosById(kudos.getTechnicalId());
      } catch (ObjectNotFoundException e) {
        LOG.debug("Kudos with id {} wasn't found", kudos.getTechnicalId(), e);
      }
    });
  }
}
