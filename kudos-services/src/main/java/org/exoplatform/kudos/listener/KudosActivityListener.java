package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import java.util.List;

/**
 * A listener to propagate comment or activity modification to Kudos stored
 * message
 */
public class KudosActivityListener extends ActivityListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(KudosActivityListener.class);

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
    if (activity != null && activity.getType().equals(KUDOS_ACTIVITY_COMMENT_TYPE)) {
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
  public void deleteActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    ExoSocialActivity activity = activityLifeCycleEvent.getSource();
    List<Kudos> linkedKudosList = kudosService.getKudosListOfActivity(activity.getId());
    if (!linkedKudosList.isEmpty()) {
      deleteLinkedKudos(linkedKudosList);
    }
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  @Override
  public void deleteComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // Same as activity processing
    deleteActivity(activityLifeCycleEvent);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    updateActivity(activityLifeCycleEvent);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    // NOT needed
  }

  private void deleteLinkedKudos(List<Kudos> linkedKudosList) {
    linkedKudosList.forEach(kudos -> {
      try {
        kudosService.deleteKudosById(kudos.getTechnicalId());
      } catch (ObjectNotFoundException e) {
        LOG.debug("Kudos with id {} wasn't found", kudos.getTechnicalId(), e);
      }
    });
  }
}
