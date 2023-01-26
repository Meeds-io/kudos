package org.exoplatform.kudos.test.listener;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.kudos.listener.ProfileUpdateListener;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.storage.api.ActivityStorage;

import java.util.ArrayList;
import java.util.List;

public class ProfileUpdateListenerTest extends BaseExoTestCase {
  private IdentityManager identityManager;

  private KudosService    kudosService;

  private ActivityStorage activityStorage;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  public void testUpdateProfileAndDetectChanges() {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root1");
    Profile profile = rootIdentity.getProfile();
    List<Integer> changes = new ArrayList<>();
    identityManager.registerProfileListener(new ProfileUpdateListener(kudosService, activityStorage) {
      @Override
      public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
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
        changes.add(1);
      }

      @Override
      public void bannerUpdated(ProfileLifeCycleEvent event) {
      }

      @Override
      public void avatarUpdated(ProfileLifeCycleEvent event) {
        changes.add(2);
      }

      @Override
      public void aboutMeUpdated(ProfileLifeCycleEvent event) {
      }
    });
    profile.setProperty(Profile.FIRST_NAME, "Changed Firstname");
    identityManager.updateProfile(profile);
    assertEquals(1, changes.size());
    assertTrue(changes.contains(1));

    profile.setProperty(Profile.ABOUT_ME, "Changed ABOUT_ME");
    profile.removeProperty(Profile.FIRST_NAME);
    identityManager.updateProfile(profile);
    assertEquals(1, changes.size());
    assertTrue(changes.contains(1));
    profile.setProperty(Profile.AVATAR, "new/avatar");
    identityManager.updateProfile(profile);
    assertEquals(2, changes.size());
    assertTrue(changes.contains(2));
  }

}
