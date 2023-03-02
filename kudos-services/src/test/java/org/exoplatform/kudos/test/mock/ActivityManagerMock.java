package org.exoplatform.kudos.test.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;

public class ActivityManagerMock implements ActivityManager {

  private Map<String, ExoSocialActivity> activities = new HashMap<>();

  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity) {
    saveActivityNoReturn(activity);
  }

  public void saveActivityNoReturn(ExoSocialActivity activity) {
    String id = String.valueOf(RandomUtils.nextLong());
    activities.put(id, activity);
    activity.setId(id);
  }

  public void saveActivity(Identity streamOwner, String type, String title) {
    // TODO Auto-generated method stub
  }

  public ExoSocialActivity getActivity(String activityId) {
    return activities.get(activityId);
  }

  public boolean isActivityViewable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    return activity != null && activity.getUserId() != null
        && ArrayUtils.contains(StringUtils.split(activity.getUserId()), viewer.getUserId());
  }

  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  public void deleteActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  public void deleteActivity(String activityId) {
    // TODO Auto-generated method stub

  }

  public void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment) {
    saveActivityNoReturn(activity);
  }

  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity, boolean loadSubComments) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteComment(String activityId, String commentId) {
    // TODO Auto-generated method stub

  }

  public void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment) {
    // TODO Auto-generated method stub

  }

  public void saveLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub

  }

  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub

  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster) {
    // TODO Auto-generated method stub
    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addProcessor(ActivityProcessor activityProcessor) {
    // TODO Auto-generated method stub

  }

  public void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin) {
    // TODO Auto-generated method stub

  }

  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {
    // TODO Auto-generated method stub

  }

  public RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    // TODO Auto-generated method stub
    return null;
  }

  public int getMaxUploadSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    // TODO Auto-generated method stub
    return false;
  }

}
