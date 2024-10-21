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
package io.meeds.kudos.listener;

import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_CANCEL_EVENT;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_GENERIC_EVENT;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_RECEIVE_KUDOS_EVENT_NAME;
import static io.meeds.kudos.service.utils.Utils.GAMIFICATION_SEND_KUDOS_EVENT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.kudos.BaseKudosTest;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.service.KudosService;

@SpringJUnitConfig(BaseKudosTest.class)
public class GamificationIntegrationListenerTest extends BaseKudosTest {

  private static final String     SENDER_REMOTE_ID                       = "root4";

  private static final AtomicLong GAMIFICATION_SEND_KUDOS_EVENT_COUNT    = new AtomicLong();

  private static final AtomicLong GAMIFICATION_RECEIVE_KUDOS_EVENT_COUNT = new AtomicLong();

  private static final AtomicLong GAMIFICATION_SAVE_KUDOS_EVENT_COUNT    = new AtomicLong();

  private static final AtomicLong GAMIFICATION_CANCEL_KUDOS_EVENT_COUNT  = new AtomicLong();

  private static final AtomicLong GAMIFICATION_LISTENER_COUNT            = new AtomicLong();

  private static boolean          listenerInstalled;

  @Autowired
  private KudosService            kudosService;

  @Autowired
  private ListenerService         listenerService;

  @BeforeEach
  @Override
  public void setUp() {
    super.setUp();
    resetCounters();
    if (!listenerInstalled) {
      Listener<Map<String, String>, String> listener = newListener();
      listenerService.addListener(GAMIFICATION_GENERIC_EVENT, listener);
      listenerService.addListener(GAMIFICATION_CANCEL_EVENT, listener);
      listenerInstalled = true;
    }
  }

  @Test
  public void testCreateKudos() throws Exception {
    createKudos();
    waitForListenerToBeCalled();

    assertEquals(1, GAMIFICATION_SEND_KUDOS_EVENT_COUNT.get());
    assertEquals(1, GAMIFICATION_RECEIVE_KUDOS_EVENT_COUNT.get());
    assertEquals(2, GAMIFICATION_SAVE_KUDOS_EVENT_COUNT.get());
    assertEquals(0, GAMIFICATION_CANCEL_KUDOS_EVENT_COUNT.get());
  }

  @Test
  public void testCancelKudosById() throws Exception {
    Kudos kudos = createKudos();
    waitForListenerToBeCalled();

    resetCounters();
    kudosService.deleteKudosById(kudos.getTechnicalId(), SENDER_REMOTE_ID);
    waitForListenerToBeCalled();

    assertEquals(1, GAMIFICATION_SEND_KUDOS_EVENT_COUNT.get());
    assertEquals(1, GAMIFICATION_RECEIVE_KUDOS_EVENT_COUNT.get());
    assertEquals(0, GAMIFICATION_SAVE_KUDOS_EVENT_COUNT.get());
    assertEquals(2, GAMIFICATION_CANCEL_KUDOS_EVENT_COUNT.get());
  }

  private Kudos createKudos() throws Exception {
    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    return kudosService.createKudos(kudos, SENDER_REMOTE_ID);
  }

  private Listener<Map<String, String>, String> newListener() {
    return new GamificationTestListener();
  }

  private void resetCounters() {
    GAMIFICATION_LISTENER_COUNT.set(0);
    GAMIFICATION_SEND_KUDOS_EVENT_COUNT.set(0);
    GAMIFICATION_RECEIVE_KUDOS_EVENT_COUNT.set(0);
    GAMIFICATION_SAVE_KUDOS_EVENT_COUNT.set(0);
    GAMIFICATION_CANCEL_KUDOS_EVENT_COUNT.set(0);
  }

  private void waitForListenerToBeCalled() {
    int tentatives = 3;
    while (tentatives-- > 0) {
      if (GAMIFICATION_LISTENER_COUNT.get() == 2) {
        break;
      } else if (GAMIFICATION_LISTENER_COUNT.get() > 2) {
        throw new IllegalStateException("Listener shouldn't be invoked more than twice, but was: " +
            GAMIFICATION_LISTENER_COUNT.get());
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    assertTrue(tentatives >= 0);
  }

  @Asynchronous
  public static class GamificationTestListener extends Listener<Map<String, String>, String> {
    @Override
    public void onEvent(Event<Map<String, String>, String> event) throws Exception {
      try {
        String eventName = event.getEventName();
        if (StringUtils.equals(eventName, GAMIFICATION_GENERIC_EVENT)) {
          GAMIFICATION_SAVE_KUDOS_EVENT_COUNT.incrementAndGet();
        } else if (StringUtils.equals(eventName, GAMIFICATION_CANCEL_EVENT)) {
          GAMIFICATION_CANCEL_KUDOS_EVENT_COUNT.incrementAndGet();
        }
        Map<String, String> gamificationMap = event.getSource();
        assertNotNull(gamificationMap);
        String ruleTitle = gamificationMap.get("ruleTitle");
        if (StringUtils.equals(ruleTitle, GAMIFICATION_SEND_KUDOS_EVENT_NAME)) {
          GAMIFICATION_SEND_KUDOS_EVENT_COUNT.incrementAndGet();
        } else if (StringUtils.equals(ruleTitle, GAMIFICATION_RECEIVE_KUDOS_EVENT_NAME)) {
          GAMIFICATION_RECEIVE_KUDOS_EVENT_COUNT.incrementAndGet();
        }
      } finally {
        GAMIFICATION_LISTENER_COUNT.incrementAndGet();
      }
    }
  }

}
