package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;

import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

/**
 * A listener to propagate comment or activity modification to Kudos stored
 * message
 */
public class KudosActivityListener extends ActivityListenerPlugin {

  private ActivityManager activityManager;

  private KudosService    kudosService;

  public KudosActivityListener(KudosService kudosService, ActivityManager activityManager) {
    this.kudosService = kudosService;
    this.activityManager = activityManager;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    ExoSocialActivity activity = activityLifeCycleEvent.getSource();
    if (activity.getType().equals(KUDOS_ACTIVITY_COMMENT_TYPE)) {
      long activityId = org.exoplatform.kudos.service.utils.Utils.getActivityId(activity.getId());
      Kudos kudos = kudosService.getKudosByActivityId(activityId);
      if (kudos != null) {
        String newMessage = activity.getTitle();
        kudos.setMessage(newMessage);
        kudosService.updateKudos(kudos);

        org.exoplatform.kudos.service.utils.Utils.computeKudosActivityProperties(activity, kudos);
        this.activityManager.updateActivity(activity, false);
      }
    }
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    ExoSocialActivity activity = activityLifeCycleEvent.getSource();
    if (activity.getType().equals(KUDOS_ACTIVITY_COMMENT_TYPE)) {
      long activityId = org.exoplatform.kudos.service.utils.Utils.getActivityId(activity.getId());
      Kudos kudos = kudosService.getKudosByActivityId(activityId);
      if (kudos != null) {
        String newMessage = activity.getTitle();
        kudos.setMessage(newMessage);
        kudosService.updateKudos(kudos);

        org.exoplatform.kudos.service.utils.Utils.computeKudosActivityProperties(activity, kudos);
        this.activityManager.updateActivity(activity, false);
      }
    }
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }
}
