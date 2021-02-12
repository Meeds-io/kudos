package org.exoplatform.kudos.test.service;

import static org.exoplatform.kudos.service.utils.Utils.toNewEntity;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.listener.GamificationIntegrationListener;
import org.exoplatform.kudos.listener.NewKudosSentActivityGeneratorListener;
import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.kudos.service.KudosStorage;
import org.exoplatform.kudos.service.utils.Utils;
import org.exoplatform.kudos.test.BaseKudosTest;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;

public class KudosServiceTest extends BaseKudosTest {
  private static final String ENTITY_TYPE        = KudosEntityType.USER_TIPTIP.name();

  private static final String ENTITY_ID          = "1";

  private static final long   RECEIVER_ID        = 3;

  private static final String RECEIVER_REMOTE_ID = "root3";

  private static final String SENDER_REMOTE_ID   = "root4";

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    KudosService kudosService = getService(KudosService.class);
    assertNotNull(kudosService);
  }

  @Test
  public void testGetKudosByEntity() {
    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);
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

    KudosService kudosService = getService(KudosService.class);
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
  public void testGetKudosByPeriodAndSender() {
    KudosService kudosService = getService(KudosService.class);
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

    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);
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
  public void testSendKudos() throws Exception {
    KudosService kudosService = getService(KudosService.class);

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
    entitiesToClean.add(kudos);

    KudosPeriod currentKudosPeriod = kudosService.getCurrentKudosPeriod();
    IdentityManager identityManager = getService(IdentityManager.class);
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
  public void testSendKudosToSpace() throws Exception {
    KudosService kudosService = getService(KudosService.class);

    String spaceRemoteId = "space3";

    Kudos kudos = newKudosDTO();
    kudos.setReceiverType(SpaceIdentityProvider.NAME);
    kudos.setReceiverId(spaceRemoteId);
    kudos.setReceiverIdentityId(null);

    restartTransaction();

    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    entitiesToClean.add(kudos);

    KudosPeriod currentKudosPeriod = kudosService.getCurrentKudosPeriod();
    IdentityManager identityManager = getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceRemoteId);
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
  }

  @Test
  public void testGetKudosByPeriodType() {
    KudosService kudosService = getService(KudosService.class);
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
    KudosService kudosService = getService(KudosService.class);

    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    assertNotNull(globalSettings);
    assertNotNull(globalSettings.getKudosPeriodType());
    assertTrue(StringUtils.isBlank(globalSettings.getAccessPermission()));
    assertTrue(globalSettings.getKudosPerPeriod() > 0);
  }

  @Test
  public void testSaveSettings() {
    KudosService kudosService = getService(KudosService.class);

    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      globalSettings.setAccessPermission("/platform");
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
  public void testSendKudosAfterLimitReached() throws Exception {
    KudosService kudosService = getService(KudosService.class);

    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      globalSettings.setKudosPerPeriod(1);
      kudosService.saveGlobalSettings(globalSettings);

      Kudos kudos = newKudosDTO();
      kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
      entitiesToClean.add(kudos);

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
  public void testGetAccountSettings() throws Exception {
    KudosService kudosService = getService(KudosService.class);

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
      entitiesToClean.add(kudos);

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
  public void testGetAccountSettingsDisabled() {
    KudosService kudosService = getService(KudosService.class);

    GlobalSettings globalSettings = kudosService.getGlobalSettings();
    GlobalSettings defaultSettings = globalSettings.clone();
    try {
      AccountSettings accountSettings = kudosService.getAccountSettings(SENDER_REMOTE_ID);
      assertNotNull(accountSettings);
      assertFalse(accountSettings.isDisabled());

      globalSettings.setAccessPermission("/platform");
      kudosService.saveGlobalSettings(globalSettings);

      AccountSettings savedAccountSettings = kudosService.getAccountSettings(SENDER_REMOTE_ID);
      assertNotNull(savedAccountSettings);
      assertTrue(savedAccountSettings.isDisabled());
      assertFalse(accountSettings.equals(savedAccountSettings));
      assertTrue(accountSettings.hashCode() != savedAccountSettings.hashCode());
    } finally {
      kudosService.saveGlobalSettings(defaultSettings);
    }
  }

  @Test
  public void testSaveKudosActivity() throws Exception {
    KudosService kudosService = getService(KudosService.class);
    ListenerService listenerService = getService(ListenerService.class);

    Kudos kudos = newKudosDTO();
    kudos = kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    entitiesToClean.add(kudos);

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
  public void testActivityCreation() throws Exception {
    KudosService kudosService = getService(KudosService.class);
    KudosStorage kudosStorage = getService(KudosStorage.class);
    ListenerService listenerService = getService(ListenerService.class);
    ActivityManager activityManager = getService(ActivityManager.class);
    ActivityStorage activityStorage = getService(ActivityStorage.class);

    listenerService.addListener(Utils.KUDOS_SENT_EVENT,
                                new NewKudosSentActivityGeneratorListener(activityManager, activityStorage));

    listenerService.addListener(Utils.KUDOS_ACTIVITY_EVENT,
                                new GamificationIntegrationListener(container, listenerService));

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
    entitiesToClean.add(kudos);

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
  public void testGetKudosByActivityId() throws Exception {
    KudosService kudosService = getService(KudosService.class);
    KudosStorage kudosStorage = getService(KudosStorage.class);
    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    kudos=kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    Kudos storedKudos=kudosStorage.getKudoById(kudos.getTechnicalId());
    entitiesToClean.add(kudos);
    KudosEntity newKudos = kudosService.getKudosByActivityId(storedKudos.getActivityId());
    assertNotNull(newKudos);
  }

  @Test
  public void testUpdateKudos() throws Exception {
    KudosService kudosService = getService(KudosService.class);
    KudosStorage kudosStorage = getService(KudosStorage.class);
    Kudos kudos = newKudosDTO();
    kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
    kudos=kudosService.createKudos(kudos, SENDER_REMOTE_ID);
    Kudos storedKudos=kudosStorage.getKudoById(kudos.getTechnicalId());
    storedKudos.setMessage("updated message");
    entitiesToClean.add(kudos);
    KudosEntity updatedKudos =toNewEntity(storedKudos);
    updatedKudos.setId(storedKudos.getTechnicalId());
    KudosEntity newKudos = kudosService.updateKudos(updatedKudos) ;
    assertEquals(newKudos.getMessage(),storedKudos.getMessage());
  }

}
