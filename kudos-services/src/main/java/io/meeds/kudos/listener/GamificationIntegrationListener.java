/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.listener;

import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_CANCEL_EVENT;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_GENERIC_EVENT;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_OBJECT_TYPE;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_RECEIVE_KUDOS_EVENT_NAME;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_SEND_KUDOS_EVENT_NAME;
import static io.meeds.kudos.service.utils.Utils.KUDOS_ACTIVITY_EVENT;
import static io.meeds.kudos.service.utils.Utils.KUDOS_CANCEL_ACTIVITY_EVENT;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

/**
 * A listener to add comment or activity
 */
@Component
@Profile("gamification")
public class GamificationIntegrationListener extends Listener<KudosService, Kudos> {

  @Autowired
  private ListenerService listenerService;

  @PostConstruct
  public void init() {
    listenerService.addListener("exo.kudos.activity", this);
    listenerService.addListener("kudos.cancel.activity", this);
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) {
    Kudos kudos = event.getData();
    String eventName = event.getEventName();

    saveSendKudosAchievement(kudos, eventName);
    saveRecieveKudosAchievement(kudos, eventName);
  }

  private void saveSendKudosAchievement(Kudos kudos, String eventName) {
    Map<String, String> gam = buildGamificationEventDetails(GAMIFICATION_SEND_KUDOS_EVENT_NAME,
                                                            kudos.getSenderId(),
                                                            kudos.getReceiverId(),
                                                            String.valueOf(kudos.getActivityId()));
    listenerService.broadcast(getGamificationEventName(eventName), gam, String.valueOf(kudos.getTechnicalId()));
  }

  private void saveRecieveKudosAchievement(Kudos kudos, String eventName) {
    Map<String, String> gam = buildGamificationEventDetails(GAMIFICATION_RECEIVE_KUDOS_EVENT_NAME,
                                                            kudos.getReceiverId(),
                                                            kudos.getSenderId(),
                                                            String.valueOf(kudos.getActivityId()));
    listenerService.broadcast(getGamificationEventName(eventName), gam, String.valueOf(kudos.getTechnicalId()));
  }

  private String getGamificationEventName(String eventName) {
    return switch (eventName) {
    case KUDOS_ACTIVITY_EVENT -> GAMIFICATION_GENERIC_EVENT;
    case KUDOS_CANCEL_ACTIVITY_EVENT -> GAMIFICATION_CANCEL_EVENT;
    default -> throw new IllegalArgumentException("Unexpected listener event name: " + eventName);
    };
  }

  private Map<String, String> buildGamificationEventDetails(String gamificationEventName,
                                                            String earnerId,
                                                            String receiverId,
                                                            String objectId) {
    Map<String, String> gam = new HashMap<>();
    gam.put("objectType", GAMIFICATION_OBJECT_TYPE);
    gam.put("objectId", objectId);
    gam.put("ruleTitle", gamificationEventName);
    gam.put("senderId", earnerId); // matches the gamification's
                                   // earner id
    gam.put("receiverId", receiverId);
    return gam;
  }

}
