package org.exoplatform.kudos.test.dao;

import static org.exoplatform.kudos.service.utils.Utils.fromEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.exoplatform.kudos.dao.KudosDAO;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.model.KudosPeriod;
import org.exoplatform.kudos.test.BaseKudosTest;

public class KudosDAOTest extends BaseKudosTest {

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    assertNotNull(kudosDAO);
  }

  @Test
  public void testGetKudosByEntity() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    List<KudosEntity> list = kudosDAO.getKudosByEntity(entityType, entityId, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByEntity(entityType, entityId, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByEntity(KudosEntityType.COMMENT.ordinal(), entityId, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosDAO.getKudosByEntity(entityType, 20, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodAndReceiver() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, true, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, true, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, false, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, 30, true, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testcountKudosByEntity() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    long count = kudosDAO.countKudosByEntity(entityType, entityId);
    assertEquals(0, count);

    newKudos();

    count = kudosDAO.countKudosByEntity(entityType, entityId);
    assertEquals(1, count);

    count = kudosDAO.countKudosByEntity(entityType, 25);
    assertEquals(0, count);
  }
  
  @Test
  public void testCountKudosByEntityAndSender() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    long count = kudosDAO.countKudosByEntityAndSender(entityType, entityId, senderId);
    assertEquals(0, count);

    newKudos();

    count = kudosDAO.countKudosByEntityAndSender(entityType, entityId, senderId);
    assertEquals(1, count);

    count = kudosDAO.countKudosByEntityAndSender(entityType, 25, senderId);
    assertEquals(0, count);
  }

  @Test
  public void testCountKudosByPeriodAndReceiver() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    long count = kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod, receiverId, true);
    assertEquals(0, count);

    newKudos();

    count = kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod, receiverId, true);
    assertEquals(1, count);

    count = kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod, receiverId, false);
    assertEquals(0, count);

    count = kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod, 30, true);
    assertEquals(0, count);
  }

  @Test
  public void testCountKudosByPeriodAndReceivers() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<Long> receiversId = new ArrayList<>();
    receiversId.add(receiverId);

    Map<Long, Long> counts = kudosDAO.countKudosByPeriodAndReceivers(kudosPeriod, receiversId);
    assertEquals(Long.valueOf(0), java.util.Optional.ofNullable(counts.get(receiverId)).orElse(0L));

    newKudos();

    receiversId.add(30L);

    counts = kudosDAO.countKudosByPeriodAndReceivers(kudosPeriod, receiversId);
    assertEquals(Long.valueOf(1), java.util.Optional.ofNullable(counts.get(receiverId)).orElse(0L));
    assertEquals(Long.valueOf(0), java.util.Optional.ofNullable(counts.get(30L)).orElse(0L));
  }

  @Test
  public void testGetKudosByPeriod() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriod(kudosPeriod, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriod(kudosPeriod, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriod(new KudosPeriod(getTime(2019, 1, 1), getTime(2019, 7, 1)), 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodAndEntityType() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriodAndEntityType(kudosPeriod, entityType, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriodAndEntityType(kudosPeriod, entityType, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriodAndEntityType(kudosPeriod, KudosEntityType.COMMENT.ordinal(), 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodAndSender() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, senderId, 10);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, senderId, 10);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, 30, 10);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testCountKudosByPeriodAndSender() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    long count = kudosDAO.countKudosByPeriodAndSender(kudosPeriod, senderId);
    assertEquals(0, count);

    newKudos();

    count = kudosDAO.countKudosByPeriodAndSender(kudosPeriod, senderId);
    assertEquals(1, count);

    count = kudosDAO.countKudosByPeriodAndSender(kudosPeriod, 30);
    assertEquals(0, count);
  }
  @Test
  public void testGetKudosByActivityId() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosEntity kudos = newKudos();
    Long activityId = 1L ;
    kudos.setActivityId(activityId);
    kudosDAO.create(kudos);
    KudosEntity newKudos = kudosDAO.getKudosByActivityId(activityId);
    compareResults(newKudos, fromEntity(kudos, DEFAULT_PORTAL));

  }
}
