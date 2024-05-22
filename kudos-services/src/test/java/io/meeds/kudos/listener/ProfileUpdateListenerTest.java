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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

import io.meeds.kudos.BaseKudosTest;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

@SpringJUnitConfig(BaseKudosTest.class)
public class ProfileUpdateListenerTest extends BaseKudosTest {

  @Mock
  private KudosService          kudosService;

  @Mock
  private CachedActivityStorage activityStorage;

  @Autowired
  private IdentityManager       identityManager;

  @Test
  public void testUpdateProfileAndDetectChanges() {
    ProfileUpdateListener profileUpdateListener = new ProfileUpdateListener(activityStorage, kudosService, identityManager);
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root1");
    Profile profile = rootIdentity.getProfile();
    identityManager.registerProfileListener(profileUpdateListener);
    Kudos kudos = new Kudos();
    kudos.setActivityId(1);
    when(kudosService.countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong())).thenReturn(1L);
    when(kudosService.getKudosByPeriodAndReceiver(anyLong(),
                                                  anyLong(),
                                                  anyLong(),
                                                  anyInt())).thenReturn(Collections.singletonList(kudos));
    doNothing().when((activityStorage)).clearActivityCached(anyString());
    profile.setProperty(Profile.FIRST_NAME, "Changed Firstname");
    identityManager.updateProfile(profile);
    verify(activityStorage, times(1)).clearActivityCached(anyString());
    verify(kudosService, times(1)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(1)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());

    profile.setProperty(Profile.ABOUT_ME, "Changed ABOUT_ME");
    profile.removeProperty(Profile.FIRST_NAME);
    identityManager.updateProfile(profile);
    verify(activityStorage, times(1)).clearActivityCached(anyString());
    verify(kudosService, times(1)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(1)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());

    profile.setProperty(Profile.AVATAR, "new/avatar");
    identityManager.updateProfile(profile);
    verify(activityStorage, times(2)).clearActivityCached(anyString());
    verify(kudosService, times(2)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(2)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());
  }
}
