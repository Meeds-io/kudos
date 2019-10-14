package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.ACTIVITY_COMMENT_ID_PREFIX;
import static org.exoplatform.kudos.service.utils.Utils.GAMIFICATION_GENERIC_EVENT;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * A listener to add comment or activity
 */
@Asynchronous
public class GamificationIntegrationListener extends Listener<KudosService, Kudos> {

  private static final Log LOG = ExoLogger.getLogger(GamificationIntegrationListener.class);

  private ListenerService  listenerService;

  private ActivityManager  activityManager;

  private PortalContainer  container;

  public GamificationIntegrationListener(PortalContainer container, ListenerService listenerService) {
    this.container = container;
    this.listenerService = listenerService;
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {

      Kudos kudos = event.getData();
      long activityId = kudos.getActivityId();
      String activityURL = "/";
      if (activityId > 0) {
        ExoSocialActivity activity = getActivityManager().getActivity(ACTIVITY_COMMENT_ID_PREFIX + activityId);
        if (activity != null) {
          activityURL = LinkProvider.getSingleActivityUrl(activity.getParentId() + "#comment-" + activity.getId());
        } else {
          activity = getActivityManager().getActivity(String.valueOf(activityId));
          if (activity != null) {
            activityURL = LinkProvider.getSingleActivityUrl(activity.getId());
          }
        }
      }

      try {
        Map<String, String> gam = new HashMap<>();
        gam.put("ruleTitle", "sendKudos");
        gam.put("object", activityURL);
        gam.put("senderId", kudos.getSenderId());
        gam.put("receiverId", kudos.getSenderId());
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }

      try {
        Map<String, String> gam = new HashMap<>();
        gam.put("ruleTitle", "receiveKudos");
        gam.put("object", activityURL);
        gam.put("senderId", kudos.getSenderId());
        gam.put("receiverId", kudos.getReceiverId());
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }

    } finally {
      RequestLifeCycle.end();
    }
  }

  public ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = CommonsUtils.getService(ActivityManager.class);
    }
    return activityManager;
  }
}
