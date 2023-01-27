package org.exoplatform.kudos.listener;

import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

import java.util.List;

public class ProfileUpdateListener extends ProfileListenerPlugin {

  private ActivityStorage activityStorage;

  private KudosService    kudosService;

  public ProfileUpdateListener(KudosService kudosService, ActivityStorage activityStorage) {
    this.kudosService = kudosService;
    this.activityStorage = activityStorage;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    String userId = event.getProfile().getIdentity().getId();
    this.clearUserActivitiesCache(userId);
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
    // NOSONAR
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    String userId = event.getProfile().getIdentity().getId();
    this.clearUserActivitiesCache(userId);
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    // NOSONAR
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    // NOSONAR
  }

  private void clearUserActivitiesCache(String userId) {
    long count = kudosService.countKudosByPeriodAndReceiver(Long.parseLong(userId), 0, System.currentTimeMillis());
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
