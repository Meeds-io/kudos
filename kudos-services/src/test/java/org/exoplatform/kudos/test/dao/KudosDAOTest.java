package org.exoplatform.kudos.test.dao;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

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

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testDeleteAll() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    try {
      kudosDAO.deleteAll();
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testDeleteAllEntities() {
    KudosDAO kudosDAO = getService(KudosDAO.class);
    try {
      kudosDAO.deleteAll(Collections.emptyList());
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testGetAllKudosByEntity() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    List<KudosEntity> list = kudosDAO.getAllKudosByEntity(entityType, entityId);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getAllKudosByEntity(entityType, entityId);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getAllKudosByEntity(KudosEntityType.COMMENT.ordinal(), entityId);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosDAO.getAllKudosByEntity(entityType, 20);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodAndReceiver() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, true);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, true);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, receiverId, false);
    assertNotNull(list);
    assertEquals(0, list.size());

    list = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod, 30, true);
    assertNotNull(list);
    assertEquals(0, list.size());
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
  public void testGetAllKudosByPeriod() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getAllKudosByPeriod(kudosPeriod);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getAllKudosByPeriod(kudosPeriod);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getAllKudosByPeriod(new KudosPeriod(getTime(2019, 1, 1), getTime(2019, 7, 1)));
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetAllKudosByPeriodAndEntityType() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getAllKudosByPeriodAndEntityType(kudosPeriod, entityType);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getAllKudosByPeriodAndEntityType(kudosPeriod, entityType);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getAllKudosByPeriodAndEntityType(kudosPeriod, KudosEntityType.COMMENT.ordinal());
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  @Test
  public void testGetKudosByPeriodAndSender() {

    KudosDAO kudosDAO = getService(KudosDAO.class);
    KudosPeriod kudosPeriod = new KudosPeriod(getTime(2019, 1, 1), getCurrentTimeInSeconds());

    List<KudosEntity> list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, senderId);
    assertNotNull(list);
    assertEquals(0, list.size());

    newKudos();

    list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, senderId);
    assertNotNull(list);
    assertEquals(1, list.size());

    list = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, 30);
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

}
