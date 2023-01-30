package org.exoplatform.kudos.test.listener;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.kudos.listener.ProfileUpdateListener;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.kudos.test.BaseKudosTest;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileUpdateListenerTest extends BaseKudosTest {

  private IdentityManager identityManager;

  private KudosService    kudosService;

  private ActivityStorage activityStorage;

  @Test
  public void testUpdateProfileAndDetectChanges() {
    identityManager =  getService(IdentityManager.class);
    kudosService = mock(KudosService.class);
    activityStorage = mock(CachedActivityStorage.class);
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root1");
    Profile profile = rootIdentity.getProfile();
    ProfileUpdateListener profileUpdateListener =new ProfileUpdateListener(kudosService, activityStorage);
    identityManager.registerProfileListener(profileUpdateListener);
    Kudos kudos = new Kudos() ;
    kudos.setActivityId(1);
    when(kudosService.countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong())).thenReturn(1L);
    when(kudosService.getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt())).thenReturn(Collections.singletonList(kudos));
    doNothing().when(((CachedActivityStorage) activityStorage)).clearActivityCached(anyString());
    profile.setProperty(Profile.FIRST_NAME, "Changed Firstname");
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(1)).clearActivityCached(anyString());
    verify(kudosService, times(1)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(1)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());

    profile.setProperty(Profile.ABOUT_ME, "Changed ABOUT_ME");
    profile.removeProperty(Profile.FIRST_NAME);
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(1)).clearActivityCached(anyString());
    verify(kudosService, times(1)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(1)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());

    profile.setProperty(Profile.AVATAR, "new/avatar");
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(2)).clearActivityCached(anyString());
    verify(kudosService, times(2)).countKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong());
    verify(kudosService, times(2)).getKudosByPeriodAndReceiver(anyLong(), anyLong(), anyLong(), anyInt());
  }
}
