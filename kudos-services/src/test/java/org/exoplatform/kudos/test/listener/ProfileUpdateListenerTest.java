package org.exoplatform.kudos.test.listener;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.kudos.listener.ProfileUpdateListener;
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
    identityManager.registerProfileListener(new ProfileUpdateListener(kudosService, activityStorage) {
      @Override
      public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void createProfile(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void technicalUpdated(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void contactSectionUpdated(ProfileLifeCycleEvent event) {
        ((CachedActivityStorage) activityStorage).clearActivityCached("1");
      }

      @Override
      public void bannerUpdated(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void avatarUpdated(ProfileLifeCycleEvent event) {
        ((CachedActivityStorage) activityStorage).clearActivityCached("2");
      }

      @Override
      public void aboutMeUpdated(ProfileLifeCycleEvent event) {
        // noop
      }
    });
    doNothing().when(((CachedActivityStorage) activityStorage)).clearActivityCached(anyString());
    profile.setProperty(Profile.FIRST_NAME, "Changed Firstname");
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(1)).clearActivityCached(anyString());

    profile.setProperty(Profile.ABOUT_ME, "Changed ABOUT_ME");
    profile.removeProperty(Profile.FIRST_NAME);
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(1)).clearActivityCached(anyString());

    profile.setProperty(Profile.AVATAR, "new/avatar");
    identityManager.updateProfile(profile);
    verify((CachedActivityStorage) activityStorage, times(2)).clearActivityCached(anyString());
  }
}
