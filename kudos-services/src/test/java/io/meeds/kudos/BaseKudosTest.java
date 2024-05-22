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
package io.meeds.kudos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import io.meeds.kernel.test.AbstractSpringTest;
import io.meeds.kernel.test.KernelExtension;
import io.meeds.kudos.dao.KudosDAO;
import io.meeds.kudos.entity.KudosEntity;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.service.utils.Utils;
import io.meeds.spring.AvailableIntegration;

@ExtendWith({ SpringExtension.class, KernelExtension.class })
@SpringBootApplication(scanBasePackages = {
    BaseKudosTest.MODULE_NAME,
    AvailableIntegration.KERNEL_TEST_MODULE,
    AvailableIntegration.JPA_MODULE,
    AvailableIntegration.LIQUIBASE_MODULE,
    AvailableIntegration.WEB_MODULE,
})
@EnableJpaRepositories(basePackages = BaseKudosTest.MODULE_NAME)
@TestPropertySource(properties = {
  "spring.liquibase.change-log=" + BaseKudosTest.CHANGELOG_PATH,
  "spring.profiles.active=gamification",
})
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/kudos-test-configuration.xml"),
})
public abstract class BaseKudosTest extends AbstractSpringTest {

  public static final String MODULE_NAME      = "io.meeds.kudos";

  public static final String CHANGELOG_PATH   = "classpath:db/changelog/kudos-rdbms.db.changelog-master.xml";

  protected KudosEntityType  kudosEntityType  = KudosEntityType.USER_TIPTIP;

  protected int              entityType       = kudosEntityType.ordinal();

  protected long             entityId         = 1;

  protected long             parentEntityId   = 2;

  protected long             receiverId       = 3;

  protected long             senderId         = 4;

  protected long             createdTimestamp = System.currentTimeMillis() / 1000;

  protected String           message          = "message";

  @BeforeEach
  public void setUp() {
    getContainer();
    begin();
  }

  @AfterEach
  public void tearDown() {
    if (getKudosDAO() != null) {
      restartTransaction();
      getKudosDAO().deleteAll();
    }
    end();
  }

  public KudosDAO getKudosDAO() {
    return getContainer().getComponentInstanceOfType(KudosDAO.class);
  }

  protected Kudos newKudosDTO() {
    KudosEntity entity = newKudosInstance(parentEntityId, entityId, entityType, receiverId, senderId, createdTimestamp, message);
    return Utils.fromEntity(entity);
  }

  protected KudosEntity newKudos() {
    return newKudos(parentEntityId, entityId, entityType, receiverId, senderId, createdTimestamp, message);
  }

  protected KudosEntity newKudosInstance() {
    return newKudosInstance(parentEntityId, entityId, entityType, receiverId, senderId, createdTimestamp, message);
  }

  protected KudosEntity newKudos(long parentEntityId,
                                 long entityId,
                                 int entityType,
                                 long receiverId,
                                 long senderId,
                                 long createdTimestamp,
                                 String message) {
    assertNotNull(getKudosDAO());
    KudosEntity kudosEntity = newKudosInstance(parentEntityId,
                                               entityId,
                                               entityType,
                                               receiverId,
                                               senderId,
                                               createdTimestamp,
                                               message);
    return getKudosDAO().save(kudosEntity);
  }

  private KudosEntity newKudosInstance(long parentEntityId,
                                       long entityId,
                                       int entityType,
                                       long receiverId,
                                       long senderId,
                                       long createdTimestamp,
                                       String message) {
    KudosEntity kudosEntity = new KudosEntity();
    kudosEntity.setEntityId(entityId);
    kudosEntity.setEntityType(entityType);
    kudosEntity.setMessage(message);
    kudosEntity.setParentEntityId(parentEntityId);
    kudosEntity.setReceiverId(receiverId);
    kudosEntity.setReceiverUser(true);
    kudosEntity.setSenderId(senderId);
    kudosEntity.setCreatedDate(createdTimestamp);
    return kudosEntity;
  }

  protected void compareResults(KudosEntity kudosEntity, Kudos kudos) {
    assertEquals(kudosEntity.getActivityId(), kudos.getActivityId());
    assertEquals(kudosEntity.getCreatedDate(), kudos.getTimeInSeconds());
    assertEquals(String.valueOf(kudosEntity.getEntityId()), kudos.getEntityId());
    assertEquals(kudosEntity.getEntityType(), KudosEntityType.valueOf(kudos.getEntityType()).ordinal());
    assertEquals(kudosEntity.getId(), kudos.getTechnicalId());
    assertEquals(kudosEntity.getMessage(), kudos.getMessage());
    assertEquals(String.valueOf(kudosEntity.getParentEntityId()), kudos.getParentEntityId());
    assertEquals(String.valueOf(kudosEntity.getReceiverId()), kudos.getReceiverIdentityId());
    assertEquals(String.valueOf(kudosEntity.getSenderId()), kudos.getSenderIdentityId());
  }

  protected long getTime(int year, int month, int day) {
    return LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
  }

  protected long getCurrentTimeInSeconds() {
    return System.currentTimeMillis() / 1000 + 10;
  }

}
