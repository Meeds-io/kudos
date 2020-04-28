/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.kudos.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

}
