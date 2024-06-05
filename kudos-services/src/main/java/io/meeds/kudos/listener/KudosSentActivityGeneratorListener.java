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

import static io.meeds.kudos.service.utils.Utils.SPACE_ACCOUNT_TYPE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.notification.Utils;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

/**
 * A listener to add comment or activity
 */
@Component
public class KudosSentActivityGeneratorListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(KudosSentActivityGeneratorListener.class);

  @Autowired
  private ActivityStorage  activityStorage;

  @Autowired
  private ActivityManager  activityManager;

  @Autowired
  private ListenerService  listenerService;

  @PostConstruct
  public void init() {
    listenerService.addListener("exo.kudos.sent", this);
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception { // NOSONAR
    Kudos kudos = event.getData();
    KudosService kudosService = event.getSource();

    if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.ACTIVITY
        || KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
      String activityId = kudos.getEntityId();
      try {
        String parentCommentId = null;
        ExoSocialActivity activity = null;
        if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
          ExoSocialActivity comment = this.activityManager.getActivity(activityId);
          if (comment != null) {
            activity = this.activityManager.getParentActivity(comment);
            if (comment.getParentCommentId() != null) {
              parentCommentId = comment.getParentCommentId();
            } else {
              parentCommentId = comment.getId();
            }
          }
        } else {
          activity = this.activityManager.getActivity(activityId);
        }
        if (activity == null) {
          throw new IllegalStateException("Activity with id '" + activityId + "' wasn't found");
        }
        ExoSocialActivity activityComment = createActivity(kudos, parentCommentId);
        activityManager.saveComment(activity, activityComment);
        long commentId = io.meeds.kudos.service.utils.Utils.getActivityId(activityComment.getId());
        kudos.setActivityId(commentId);
        kudosService.updateKudosGeneratedActivityId(kudos.getTechnicalId(), kudos.getActivityId());

        clearActivityCached(activity.getId());
        clearActivityCached(activityComment.getId());
      } catch (Exception e) {
        LOG.warn("Error adding comment on activity with id '" + activityId + "' for Kudos with id " + kudos.getTechnicalId(), e);
      }
    } else {
      ExoSocialActivity activity = createActivity(kudos, null);
      String providerId = OrganizationIdentityProvider.NAME;
      String remoteId = kudos.getReceiverId();
      boolean isSpaceActivity = StringUtils.isNotBlank(kudos.getSpacePrettyName());
      if (SPACE_ACCOUNT_TYPE.equals(kudos.getReceiverType()) || isSpaceActivity) {
        providerId = SpaceIdentityProvider.NAME;
        if (isSpaceActivity) {
          remoteId = kudos.getSpacePrettyName();
        }
      }

      Identity owner = Utils.getIdentityManager().getOrCreateIdentity(providerId, remoteId);
      if (owner == null) {
        LOG.warn("Can't find receiver identity with type/id", kudos.getReceiverType(), remoteId);
      } else {
        activityManager.saveActivityNoReturn(owner, activity);
        kudosService.updateKudosGeneratedActivityId(kudos.getTechnicalId(),
                                                    io.meeds.kudos.service.utils.Utils.getActivityId(activity.getId()));
        clearActivityCached(activity.getId());
      }
    }
  }

  private ExoSocialActivity createActivity(Kudos kudos, String parentCommentId) {
    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setParentCommentId(parentCommentId);
    activity.setType(io.meeds.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE);
    activity.setTitle(kudos.getMessage());
    activity.setBody("Kudos to " + kudos.getReceiverFullName());
    activity.setUserId(kudos.getSenderIdentityId());
    io.meeds.kudos.service.utils.Utils.computeKudosActivityProperties(activity, kudos);
    return activity;
  }

  private void clearActivityCached(String id) {
    if (activityStorage instanceof CachedActivityStorage cachedActivityStorage) {
      cachedActivityStorage.clearActivityCached(id);
    }
  }

}
