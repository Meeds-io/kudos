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
package io.meeds.kudos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.kudos.BaseKudosTest;
import io.meeds.kudos.dao.KudosDAO;
import io.meeds.kudos.entity.KudosEntity;
import io.meeds.kudos.model.AccountSettings;
import io.meeds.kudos.model.GlobalSettings;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.service.utils.Utils;
import io.meeds.kudos.storage.KudosStorage;
import io.meeds.test.kudos.mock.SpaceServiceMock;

import lombok.SneakyThrows;

@SpringJUnitConfig(BaseKudosTest.class)
public class KudosServiceTest extends BaseKudosTest {

  private static final String ENTITY_TYPE        = KudosEntityType.USER_TIPTIP.name();

  private static final String ENTITY_ID          = "1";

  private static final long   RECEIVER_ID        = 3;

  private static final String RECEIVER_REMOTE_ID = "root3";

  private static final String SENDER_REMOTE_ID   = "root4";

  @Autowired
  IdentityManager             identityManager;

  @Autowired
  ListenerService             listenerService;

  @Autowired
  KudosStorage                kudosStorage;

  @Autowired
  KudosService                kudosService;

  @Autowired
  SpaceService                spaceService;

  @Autowired
  ActivityManager             activityManager;

  @Autowired
  KudosDAO                    kudosDAO;

  @Override
  public KudosDAO getKudosDAO() {
    return kudosDAO;
  }

  @Test
  public void testGetKudosByEntity() {
    List<Kudos> list = kudosService.getKudosByEntity(ENTITY_TYPE, ENTITY_ID, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByEntity(ENTITY_TYPE, ENTITY_ID, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByEntity(KudosEntityType.SPACE_TIPTIP.name(), ENTITY_ID, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosService.getKudosByEntity(ENTITY_TYPE, "20", 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testCountKudosByEntity() {
    long count = kudosService.countKudosByEntity(ENTITY_TYPE, ENTITY_ID);
    assertEquals(0, count);

    newKudos();

    count = kudosService.countKudosByEntity(ENTITY_TYPE, ENTITY_ID);
    assertEquals(1, count);

    count = kudosService.countKudosByEntity(ENTITY_TYPE, "25");
    assertEquals(0, count);
  }

  @Test
  public void testCountKudosByEntityAndSender() {
    long count = kudosService.countKudosByEntityAndSender(ENTITY_TYPE, ENTITY_ID, String.valueOf(senderId));
    assertEquals(0, count);

    newKudos();

    count = kudosService.countKudosByEntityAndSender(ENTITY_TYPE, ENTITY_ID, String.valueOf(senderId));
    assertEquals(1, count);

    count = kudosService.countKudosByEntityAndSender(ENTITY_TYPE, "25", String.valueOf(senderId));
    assertEquals(0, count);
  }

  @Test
  public void testGetKudosByPeriodAndReceiver() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    List<Kudos> list = kudosService.getKudosByPeriodAndReceiver(RECEIVER_ID, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByPeriodAndReceiver(RECEIVER_ID, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByPeriodAndReceiver(30, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosService.getKudosByPeriodAndReceiver(30000, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testCountKudosByPeriodAndReceiver() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    long count = kudosService.countKudosByPeriodAndReceiver(RECEIVER_ID, startTime, endTime);
    assertEquals(0, count);

    newKudos();

    count = kudosService.countKudosByPeriodAndReceiver(RECEIVER_ID, startTime, endTime);
    assertEquals(1, count);

    count = kudosService.countKudosByPeriodAndReceiver(30, startTime, endTime);
    assertEquals(0, count);

    count = kudosService.countKudosByPeriodAndReceiver(30000, startTime, endTime);
    assertEquals(0, count);
  }

  @Test
  public void testCountKudosByPeriodAndReceivers() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    List<Long> receivers = new ArrayList<>();
    receivers.add(RECEIVER_ID);

    Map<Long, Long> counts = kudosService.countKudosByPeriodAndReceivers(receivers, startTime, endTime);
    assertEquals(Long.valueOf(0), java.util.Optional.ofNullable(counts.get(RECEIVER_ID)).orElse(0L));

    newKudos();

    receivers.add(30L);
    receivers.add(30000L);

    counts = kudosService.countKudosByPeriodAndReceivers(receivers, startTime, endTime);
    assertEquals(Long.valueOf(1), java.util.Optional.ofNullable(counts.get(RECEIVER_ID)).orElse(0L));
    assertEquals(Long.valueOf(0), java.util.Optional.ofNullable(counts.get(30L)).orElse(0L));
    assertEquals(Long.valueOf(0), java.util.Optional.ofNullable(counts.get(30000L)).orElse(0L));
  }

  @Test
  public void testGetKudosByPeriodAndSender() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    List<Kudos> list = kudosService.getKudosByPeriodAndSender(senderId, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByPeriodAndSender(senderId, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByPeriodAndSender(30, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosService.getKudosByPeriodAndSender(30000, startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testCountKudosByPeriodAndSender() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    long count = kudosService.countKudosByPeriodAndSender(senderId, startTime, endTime);
    assertEquals(0, count);

    newKudos();

    count = kudosService.countKudosByPeriodAndSender(senderId, startTime, endTime);
    assertEquals(1, count);

    count = kudosService.countKudosByPeriodAndSender(30, startTime, endTime);
    assertEquals(0, count);

    count = kudosService.countKudosByPeriodAndSender(30000, startTime, endTime);
    assertEquals(0, count);
  }

  @Test
  public void testGetKudosByPeriod() {
    long startTime = getTime(2019, 1, 1);
    long endTime = getCurrentTimeInSeconds();

    List<Kudos> list = kudosService.getKudosByPeriod(startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByPeriod(startTime, endTime, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByPeriod(getTime(2019, 1, 1), getTime(2019, 7, 1), 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodOfDate() {
    long startTime = getCurrentTimeInSeconds();

    List<Kudos> list = kudosService.getKudosByPeriodOfDate(startTime, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByPeriodOfDate(startTime, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByPeriodOfDate(getTime(2019, 6, 1), 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  @SneakyThrows
  public void testSendKudos() {
    Kudos kudos = newKudosDTO();
    try {
      kudosService.createKudos(kudos, RECEIVER_REMOTE_ID);
      fail("Sender shouldn't be able to send kudos to himself");
    } catch (Exception e) {
      // Expected
    }

    // Same receiver and sender
    try {
      Kudos fakeKudos = newKudosDTO();
      fakeKudos.setReceiverId(SENDER_REMOTE_ID);
      kudosService.createKudos(fakeKudos, SENDER_REMOTE_ID);
      fail("Sender shouldn't be the same as sender in DTO");
    } catch (Exception e) {
      // Expected
    }

    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);

    KudosPeriod currentKudosPeriod = kudosService.getCurrentKudosPeriod();
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER_REMOTE_ID);
    List<Kudos> list = kudosService.getKudosByPeriodAndReceiver(Long.parseLong(identity.getId()),
                                                                currentKudosPeriod.getStartDateInSeconds(),
                                                                currentKudosPeriod.getEndDateInSeconds(),
                                                                10);
    assertNotNull(list);
    assertEquals(1, list.size());
    Kudos retrievedKudos = list.get(0);
    assertEquals(kudos, retrievedKudos);
    assertEquals(kudos.getTechnicalId(), retrievedKudos.getTechnicalId());
    assertEquals(kudos.getTimeInSeconds(), retrievedKudos.getTimeInSeconds());
    assertEquals(kudos.hashCode(), retrievedKudos.hashCode());
    assertNotNull(kudos.toString());
    assertTrue(kudos.toString().contains(SENDER_REMOTE_ID));
    assertTrue(kudos.toString().contains(RECEIVER_REMOTE_ID));
  }

  @Test
  @SneakyThrows
  public void testSendKudosToSpace() {
    String spaceRemoteId = "space3";

    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(spaceRemoteId);
    KudosPeriod currentKudosPeriod = kudosService.getCurrentKudosPeriod();

    Kudos kudosToSend = newKudosDTO();
    kudosToSend.setReceiverType(SpaceIdentityProvider.NAME);
    kudosToSend.setReceiverId(spaceRemoteId);
    kudosToSend.setReceiverIdentityId(null);

    restartTransaction();

    assertThrows(IllegalAccessException.class, () -> kudosService.createKudos(kudosToSend, SENDER_REMOTE_ID));

    List<Kudos> list = kudosService.getKudosByPeriodAndReceiver(Long.parseLong(spaceIdentity.getId()),
                                                                currentKudosPeriod.getStartDateInSeconds(),
                                                                currentKudosPeriod.getEndDateInSeconds(),
                                                                10);
    assertNotNull(list);
    assertEquals(0, list.size());

    SpaceServiceMock.setRedactor(SENDER_REMOTE_ID);
    Kudos kudos;
    try {
      kudos = kudosService.createKudos(kudosToSend, SENDER_REMOTE_ID);
    } finally {
      SpaceServiceMock.setRedactor(null);
    }
    list = kudosService.getKudosByPeriodAndReceiver(Long.parseLong(spaceIdentity.getId()),
                                                    currentKudosPeriod.getStartDateInSeconds(),
                                                    currentKudosPeriod.getEndDateInSeconds(),
                                                    10);
    assertNotNull(list);
    assertEquals(1, list.size());
    Kudos retrievedKudos = list.get(0);
    assertEquals(kudos, retrievedKudos);
    assertEquals(kudos.getTechnicalId(), retrievedKudos.getTechnicalId());
    assertEquals(kudos.getTimeInSeconds(), retrievedKudos.getTimeInSeconds());
    assertEquals(kudos.hashCode(), retrievedKudos.hashCode());

    Kudos kudosToSendOnActivity = newKudosDTO();
    kudosToSendOnActivity.setReceiverType(SpaceIdentityProvider.NAME);
    kudosToSendOnActivity.setReceiverId(spaceRemoteId);
    kudosToSendOnActivity.setReceiverIdentityId(null);
    kudosToSendOnActivity.setReceiverIdentityId(null);
    kudosToSendOnActivity.setEntityType(KudosEntityType.ACTIVITY.name());
    kudosToSendOnActivity.setEntityId(String.valueOf(retrievedKudos.getActivityId()));

    SpaceServiceMock.setMember(SENDER_REMOTE_ID);
    try {
      kudosService.createKudos(kudosToSendOnActivity, SENDER_REMOTE_ID);
    } finally {
      SpaceServiceMock.setMember(null);
    }

    list = kudosService.getKudosByPeriodAndReceiver(Long.parseLong(spaceIdentity.getId()),
                                                    currentKudosPeriod.getStartDateInSeconds(),
                                                    currentKudosPeriod.getEndDateInSeconds(),
                                                    10);
    assertNotNull(list);
    assertEquals(2, list.size());

    retrievedKudos = list.get(0);
    assertEquals(kudosToSendOnActivity.getEntityType(), retrievedKudos.getEntityType());
    assertEquals(kudosToSendOnActivity.getEntityId(), retrievedKudos.getEntityId());
  }

  @Test
  public void testGetKudosByPeriodType() {
    long startTime = getCurrentTimeInSeconds();

    try {
      kudosService.getKudosByPeriod(startTime, null, 10);
      fail("Shouldn't be able to get kudos list by null period type");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    List<Kudos> list = kudosService.getKudosByPeriod(startTime, KudosPeriodType.WEEK, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    KudosEntity kudosEntity = newKudos();

    list = kudosService.getKudosByPeriod(startTime, KudosPeriodType.WEEK, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    Kudos kudos = list.get(0);
    compareResults(kudosEntity, kudos);

    list = kudosService.getKudosByPeriod(getTime(2019, 6, 1), KudosPeriodType.WEEK, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGlobalSettings() {
    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    assertNotNull(globalSettings);
    assertNotNull(globalSettings.getKudosPeriodType());
    assertTrue(StringUtils.isBlank(globalSettings.getAccessPermission()));
    assertTrue(globalSettings.getKudosPerPeriod() > 0);
  }

  @Test
  public void testSaveSettings() {
    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      globalSettings.setKudosPeriodType(KudosPeriodType.WEEK);
      globalSettings.setKudosPerPeriod(1);
      kudosService.saveGlobalSettings(globalSettings);

      GlobalSettings savedGlobalSettings = kudosService.getGlobalSettings();

      assertEquals(globalSettings, savedGlobalSettings);
      assertEquals(globalSettings.hashCode(), savedGlobalSettings.hashCode());
    } finally {
      kudosService.saveGlobalSettings(defaultSettings);
    }
  }

  @Test
  @SneakyThrows
  public void testSendKudosAfterLimitReached() {
    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      globalSettings.setKudosPerPeriod(1);
      kudosService.saveGlobalSettings(globalSettings);

      Kudos kudos = newKudosDTO();
      kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);

      // Time is stored in seconds, thus, we have to wait a second
      Thread.sleep(1000);

      try {
        kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
        fail("Shouldn't be able to send another Kudos");
      } catch (Exception e) {
        // Expected
      }
    } finally {
      kudosService.saveGlobalSettings(defaultSettings);
    }
  }

  @Test
  @SneakyThrows
  public void testGetAccountSettings() {
    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      AccountSettings accountSettings = kudosService.getAccountSettings(SENDER_REMOTE_ID);
      assertNotNull(accountSettings);
      assertFalse(accountSettings.isDisabled());
      assertEquals(globalSettings.getKudosPerPeriod(), accountSettings.getRemainingKudos());

      globalSettings.setKudosPerPeriod(1);
      kudosService.saveGlobalSettings(globalSettings);

      Kudos kudos = newKudosDTO();
      kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);

      // Time is stored in seconds, thus, we have to wait a second
      Thread.sleep(1000);

      accountSettings = kudosService.getAccountSettings(SENDER_REMOTE_ID);
      assertNotNull(accountSettings);
      assertFalse(accountSettings.isDisabled());
      assertEquals(0, accountSettings.getRemainingKudos());
    } finally {
      kudosService.saveGlobalSettings(defaultSettings);
    }
  }

  @Test
  @SneakyThrows
  public void testSaveKudosActivity() {
    Kudos kudos = newKudosDTO();
    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);

    final AtomicBoolean listenerInvoked = new AtomicBoolean(false);
    listenerService.addListener(Utils.KUDOS_ACTIVITY_EVENT, new Listener<KudosService, Kudos>() {
      @Override
      public void onEvent(Event<KudosService, Kudos> event) throws Exception {
        listenerInvoked.set(true);
      }
    });

    kudosService.updateKudosGeneratedActivityId(kudos.getTechnicalId(), kudos.getActivityId());
    assertTrue(listenerInvoked.get());
  }

  @Test
  @SneakyThrows
  public void testActivityCreation() {
    final AtomicBoolean listenerInvoked = new AtomicBoolean(false);
    listenerService.addListener(Utils.GAMIFICATION_GENERIC_EVENT, new Listener<KudosService, Kudos>() {
      @Override
      public void onEvent(Event<KudosService, Kudos> event) throws Exception {
        listenerInvoked.set(true);
      }
    });

    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);

    kudos = kudosStorage.getKudoById(kudos.getTechnicalId());
    assertTrue(kudos.getActivityId() > 0);

    // Wait async listener gets invoked
    int i = 0;
    while (i < 10 && !listenerInvoked.get()) {
      Thread.sleep(500);
      i++;
    }
    assertTrue(listenerInvoked.get());
  }

  @Test
  @SneakyThrows
  public void testGetKudosByActivityId() { // NOSONAR
                                           // comparaison is
                                           // made in private
                                           // method
    KudosEntity kudosEntity = newKudos();
    kudosEntity.setEntityType(KudosEntityType.USER_PROFILE.ordinal());
    Kudos kudos = kudosService.createKudos(Utils.fromEntity(kudosEntity), SENDER_REMOTE_ID);
    Kudos storedKudos = kudosStorage.getKudoById(kudos.getTechnicalId());
    Kudos newKudos = kudosService.getKudosByActivityId(storedKudos.getActivityId());
    compareResults(Utils.toEntity(storedKudos), newKudos);
  }

  @Test
  @SneakyThrows
  public void testGetKudosListOfActivity() {
    resetGlobalSettings();

    KudosEntity kudosEntity = newKudosInstance();
    kudosEntity.setEntityType(KudosEntityType.SPACE_PROFILE.ordinal());
    Kudos parentKudos = kudosService.createKudos(Utils.fromEntity(kudosEntity), SENDER_REMOTE_ID);

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setUserId("root");
    activityManager.saveActivityNoReturn(activity);
    parentKudos.setActivityId(Long.parseLong(activity.getId()));
    kudosService.updateKudosGeneratedActivityId(parentKudos.getTechnicalId(), Long.parseLong(activity.getId()));

    KudosEntity childKudosEntity = newKudosInstance();
    childKudosEntity.setEntityType(KudosEntityType.ACTIVITY.ordinal());
    childKudosEntity.setEntityId(250l);
    childKudosEntity.setParentEntityId(parentKudos.getActivityId());
    Kudos childKudos = kudosService.createKudos(Utils.fromEntity(childKudosEntity), SENDER_REMOTE_ID);

    try { // NOSONAR Test expected exception
      kudosService.getKudosListOfActivity(activity.getId(), null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    List<Kudos> kudosList = kudosService.getKudosListOfActivity("2556",
                                                                new org.exoplatform.services.security.Identity("root"));
    assertNotNull(kudosList);
    assertTrue(kudosList.isEmpty());

    try {
      kudosService.getKudosListOfActivity(activity.getId(),
                                          new org.exoplatform.services.security.Identity("root4"));
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }

    kudosList = kudosService.getKudosListOfActivity(activity.getId(),
                                                    new org.exoplatform.services.security.Identity("root"));

    assertNotNull(kudosList);
    assertEquals(2, kudosList.size());
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == parentKudos.getTechnicalId()));
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == childKudos.getTechnicalId()));

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setUserId("root,root4");
    activityManager.saveActivityNoReturn(comment);
    childKudos.setActivityId(Long.parseLong(comment.getId()));
    kudosService.updateKudosGeneratedActivityId(childKudos.getTechnicalId(), Long.parseLong(comment.getId()));

    KudosEntity subCommentKudosEntity = newKudosInstance();
    subCommentKudosEntity.setEntityType(KudosEntityType.COMMENT.ordinal());
    subCommentKudosEntity.setEntityId(255l);
    subCommentKudosEntity.setParentEntityId(parentKudos.getActivityId());
    Kudos subCommentKudos = kudosService.createKudos(Utils.fromEntity(subCommentKudosEntity), SENDER_REMOTE_ID);

    kudosList = kudosService.getKudosListOfActivity(activity.getId(),
                                                    new org.exoplatform.services.security.Identity("root"));

    assertNotNull(kudosList);
    assertEquals(3, kudosList.size());
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == parentKudos.getTechnicalId()));
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == childKudos.getTechnicalId()));
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == subCommentKudos.getTechnicalId()));
  }

  @Test
  @SneakyThrows
  public void testUpdateKudos() {
    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    Kudos storedKudos = kudosStorage.getKudoById(kudos.getTechnicalId());
    storedKudos.setMessage("updated message");
    Kudos newKudos = kudosService.updateKudos(storedKudos);
    assertEquals(newKudos.getMessage(), storedKudos.getMessage());
    compareResults(Utils.toEntity(newKudos), storedKudos);
  }

  @Test
  @SneakyThrows
  public void testDeleteKudosById() {
    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    long kudosId = kudos.getTechnicalId();

    assertThrows(IllegalArgumentException.class, () -> kudosService.deleteKudosById(0, "root4"));
    assertThrows(ObjectNotFoundException.class, () -> kudosService.deleteKudosById(100, "root4"));
    assertThrows(IllegalAccessException.class, () -> kudosService.deleteKudosById(kudosId, "root3"));
    kudosService.deleteKudosById(kudosId, "root4");
    Kudos kudos1 = kudosStorage.getKudoById(kudos.getTechnicalId());
    assertNull(kudos1);
  }

  private void resetGlobalSettings() {
    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    globalSettings.setKudosPeriodType(KudosPeriodType.WEEK);
    globalSettings.setKudosPerPeriod(100);
    kudosService.saveGlobalSettings(globalSettings);
  }
}
