package org.exoplatform.addon.kudos.listener;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * A listener to add comment or activity
 */
public class IntegrationGamificationListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(IntegrationGamificationListener.class);
  
  private ListenerService  listenerService;

  public IntegrationGamificationListener(ListenerService  listenerService) {
    this.listenerService = listenerService;
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    
 
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", "sendKudos");
      gam.put("object", "/portal/intranet/");
      gam.put("senderId", kudos.getSenderId());
      gam.put("receiverId", kudos.getSenderId());
      listenerService.broadcast("exo.gamification.generic.action", gam, "");
  } catch (Exception e) {
    LOG.error("Cannot broadcast gamification event");
  }
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", "receiveKudos");
      gam.put("object", "/portal/intranet/");
      gam.put("senderId", kudos.getSenderId());
      gam.put("receiverId", kudos.getReceiverId());
      listenerService.broadcast("exo.gamification.generic.action", gam, "");
  } catch (Exception e) {
    LOG.error("Cannot broadcast gamification event");
  }
    
  }


}
