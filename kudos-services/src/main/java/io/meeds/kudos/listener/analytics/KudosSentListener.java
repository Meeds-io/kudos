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
package io.meeds.kudos.listener.analytics;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;
import static org.exoplatform.analytics.utils.AnalyticsUtils.getIdentity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

@Asynchronous
@Component
@Profile("analytics")
public class KudosSentListener extends Listener<KudosService, Kudos> {

  private static final Log LOG = ExoLogger.getLogger(KudosSentListener.class);

  @Autowired
  private ActivityManager  activityManager;

  @Autowired
  private SpaceService     spaceService;

  @Autowired
  private ListenerService  listenerService;

  @PostConstruct
  public void init() {
    listenerService.addListener("exo.kudos.activity", this);
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception { // NOSONAR
    Kudos kudos = event.getData();
    long activityId = kudos.getActivityId();
    long streamIdentityId = 0;
    boolean receiverChanged = false;

    if (activityId <= 0
        && (StringUtils.equals("ACTIVITY", kudos.getEntityType()) || StringUtils.equals("COMMENT", kudos.getEntityType()))) {
      activityId = Long.parseLong(kudos.getEntityId());
    }
    StatisticData statisticData = new StatisticData();

    if (activityId > 0) {
      ExoSocialActivity activity = activityManager.getActivity(RDBMSActivityStorageImpl.COMMENT_PREFIX + activityId);
      if (activity == null) {
        activity = activityManager.getActivity(String.valueOf(activityId));
      }
      if (activity != null) {
        ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
        if (parentActivity != null) {
          activity = parentActivity;
        }
      }
      Identity streamIdentity = null;
      if (activity != null) {
        if (!activity.getPosterId().equals(kudos.getReceiverIdentityId())) {
          receiverChanged = true;
        }
        ActivityStream activityStream = activity.getActivityStream();
        if (activityStream != null) {
          Type type = activityStream.getType();
          boolean isSpace = type == Type.SPACE;
          String streamProviderId = isSpace ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
          String streamRemoteId = activityStream.getPrettyId();
          try {
            streamIdentity = getIdentity(streamProviderId, streamRemoteId);
          } catch (Exception e) {
            LOG.debug("Can't retrieve identity with providerId {} and remoteId {}. Attempt to retrieve it as Identity technical ID",
                      streamProviderId,
                      streamRemoteId,
                      e);
            streamIdentity = getIdentity(activityStream.getId());
          }
        }

      }

      if (streamIdentity != null) {
        streamIdentityId = Long.parseLong(streamIdentity.getId());
        if (StringUtils.equals(streamIdentity.getProviderId(), SpaceIdentityProvider.NAME)) {
          Space space = spaceService.getSpaceByPrettyName(streamIdentity.getRemoteId());
          addSpaceStatistics(statisticData, space);
        }
      }
    }
    // kudos sent for a user in a chosen audience
    if (kudos.getSpacePrettyName() != null) {
      Space space = spaceService.getSpaceByPrettyName(kudos.getSpacePrettyName());
      Identity spaceIdentity = getIdentity(SpaceIdentityProvider.NAME, kudos.getSpacePrettyName());
      if (spaceIdentity != null) {
        streamIdentityId = Long.parseLong(spaceIdentity.getId());
        addSpaceStatistics(statisticData, space);
      }
    }

    statisticData.setModule("social");
    statisticData.setSubModule("kudos");
    statisticData.setOperation("sendKudos");
    statisticData.setUserId(Long.parseLong(kudos.getSenderIdentityId()));
    statisticData.addParameter("activityId", activityId);
    statisticData.addParameter("streamIdentityId", streamIdentityId);
    statisticData.addParameter("kudosId", kudos.getTechnicalId());
    statisticData.addParameter("senderId", kudos.getSenderIdentityId());
    statisticData.addParameter("receiverId", kudos.getReceiverIdentityId());
    statisticData.addParameter("entityId", kudos.getEntityId());
    statisticData.addParameter("entityType", kudos.getEntityType());
    statisticData.addParameter("parentEntityId", kudos.getParentEntityId());
    statisticData.addParameter("receiverType", kudos.getReceiverType());
    statisticData.addParameter("messageLength", kudos.getMessage().length());
    statisticData.addParameter("duration", kudos.getTimeInSeconds());
    statisticData.addParameter("receiverChanged", receiverChanged);

    AnalyticsUtils.addStatisticData(statisticData);
  }

}
