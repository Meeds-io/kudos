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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateListener extends ProfileListenerPlugin {

  @Autowired
  private ActivityStorage activityStorage;

  @Autowired
  private KudosService    kudosService;

  @Autowired
  private IdentityManager identityManager;

  @PostConstruct
  public void init() {
    identityManager.registerProfileListener(this);
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    clearUserActivitiesCache(event);
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    clearUserActivitiesCache(event);
  }

  private void clearUserActivitiesCache(ProfileLifeCycleEvent event) {
    String userId = event.getProfile().getIdentity().getId();
    this.clearUserActivitiesCache(userId);
  }

  private void clearUserActivitiesCache(String userId) {
    long count = kudosService.countKudosByPeriodAndReceiver(Long.parseLong(userId), 0, System.currentTimeMillis());
    if (count > 0) {
      List<Kudos> kudosList = kudosService.getKudosByPeriodAndReceiver(Long.parseLong(userId),
                                                                       0,
                                                                       System.currentTimeMillis(),
                                                                       (int) count);
      if (kudosList == null || kudosList.isEmpty())
        return;
      kudosList.stream()
               .forEach(kudos -> ((CachedActivityStorage) activityStorage).clearActivityCached(String.valueOf(kudos.getActivityId())));
    }
  }

}
