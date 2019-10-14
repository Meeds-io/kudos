package org.exoplatform.kudos.test.mock;

import java.util.List;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;

public class ActivityManagerMock implements ActivityManager {

  @Override
  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity) {
    // TODO Auto-generated method stub
  }

  @Override
  public void saveActivityNoReturn(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveActivity(Identity streamOwner, String type, String title) {
    // TODO Auto-generated method stub

  }

  @Override
  public ExoSocialActivity getActivity(String activityId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteActivity(String activityId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment) {
    // TODO Auto-generated method stub

  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity, boolean loadSubComments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteComment(String activityId, String commentId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub

  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addProcessor(ActivityProcessor activityProcessor) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {
    // TODO Auto-generated method stub

  }

  @Override
  public ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExoSocialActivity saveActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity,
                                                            int offset,
                                                            int length) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExoSocialActivity recordActivity(Identity owner,
                                          String type,
                                          String title,
                                          String body) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getActivitiesCount(Identity owner) throws ActivityStorageException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void processActivitiy(ExoSocialActivity activity) {
    // TODO Auto-generated method stub

  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getMaxUploadSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    // TODO Auto-generated method stub
    return false;
  }

}
