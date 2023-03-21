package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.GAMIFICATION_GENERIC_EVENT;
import static org.exoplatform.kudos.service.utils.Utils.KUDOS_ACTIVITY_EVENT;
import static org.exoplatform.kudos.service.utils.Utils.KUDOS_CANCEL_ACTIVITY_EVENT;
import static org.exoplatform.kudos.service.utils.Utils.GAMIFICATION_CANCEL_EVENT;

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
import org.exoplatform.social.core.manager.ActivityManager;

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
  public void onEvent(Event<KudosService, Kudos> event) {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      Kudos kudos = event.getData();
      try {
        Map<String, String> gam = new HashMap<>();
        gam.put("ruleTitle", "sendKudos");
        gam.put("objectId", String.valueOf(kudos.getActivityId()));
        gam.put("objectType", "activity");
        gam.put("senderId", kudos.getSenderId()); // matches the gamification's earner id
        gam.put("receiverId", kudos.getReceiverId());
        switch (event.getEventName()) {
        case KUDOS_ACTIVITY_EVENT: {
          listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
          break;
        }
        case KUDOS_CANCEL_ACTIVITY_EVENT: {
          listenerService.broadcast(GAMIFICATION_CANCEL_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
          getActivityManager().deleteActivity(String.valueOf(kudos.getActivityId()));
          break;
        }
        default:
          throw new IllegalArgumentException("Unexpected listener event name: " + event.getEventName());
        }
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }

      try {
        Map<String, String> gam = new HashMap<>();
        gam.put("ruleTitle", "receiveKudos");
        gam.put("objectId", String.valueOf(kudos.getActivityId()));
        gam.put("objectType", "activity");
        gam.put("senderId", kudos.getReceiverId()); // matches the gamification's earner id
        gam.put("receiverId", kudos.getSenderId());
        switch (event.getEventName()) {
        case KUDOS_ACTIVITY_EVENT: {
          listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
          break;
        }
        case KUDOS_CANCEL_ACTIVITY_EVENT: {
          listenerService.broadcast(GAMIFICATION_CANCEL_EVENT, gam, String.valueOf(kudos.getTechnicalId()));
          break;
        }
        default:
          throw new IllegalArgumentException("Unexpected listener event name: " + event.getEventName());
        }
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
