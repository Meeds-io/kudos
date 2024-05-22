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
package io.meeds.test.kudos.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;

public class ActivityStorageMock implements ActivityStorage {

  private AtomicInteger                  idGenerator = new AtomicInteger(1);

  private Map<String, ExoSocialActivity> activities  = new HashMap<>();

  @Override
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {
    return activities.get(activityId);
  }

  @Override
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ExoSocialActivity> getUserActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getUserIdsActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(Identity owner,
                                                             long offset,
                                                             long limit) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ExoSocialActivity> getActivities(Identity owner,
                                               Identity viewer,
                                               long offset,
                                               long limit) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException {
    activity.setId(String.valueOf(idGenerator.getAndIncrement()));
    activities.put(activity.getId(), activity);
    return activity;
  }

  @Override
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteActivity(String activityId) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNumberOfUserActivitiesForUpgrade(Identity owner) throws ActivityStorageException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<String> getActivityIdsFeed(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivityFeedForUpgrade(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<String> getActivityIdsOfConnections(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(Identity ownerIdentity,
                                                                   ExoSocialActivity baseActivity,
                                                                   long limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(Identity ownerIdentity,
                                                                   ExoSocialActivity baseActivity,
                                                                   int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<String> getUserSpacesActivityIds(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(Identity ownerIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(Identity ownerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity,
                                                                int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity,
                                                                int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, boolean loadSubComments, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfComments(ExoSocialActivity existingActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public SortedSet<ActivityProcessor> getActivityProcessors() {
    throw new UnsupportedOperationException();

  }

  @Override
  public void updateActivity(ExoSocialActivity existingActivity) throws ActivityStorageException {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfSpaceActivities(Identity spaceIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(Identity spaceIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getSpaceActivities(Identity spaceIdentity, int index, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<String> getSpaceActivityIds(Identity spaceIdentity, int index, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int index, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfActivitiesByPoster(Identity ownerIdentity, Identity viewerIdentity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerFeedActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity ownerIdentity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, Long sinceTime) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getAllActivities(int index, int limit) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    throw new UnsupportedOperationException();

  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    throw new UnsupportedOperationException();
  }

}
