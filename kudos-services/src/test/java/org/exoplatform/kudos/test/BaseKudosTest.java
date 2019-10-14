package org.exoplatform.kudos.test;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.kudos.dao.KudosDAO;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.service.utils.Utils;

public abstract class BaseKudosTest {

  protected static PortalContainer container;

  protected int                    entityType       = KudosEntityType.USER_TIPTIP.ordinal();

  protected long                   entityId         = 1;

  protected long                   parentEntityId   = 2;

  protected long                   receiverId       = 3;

  protected long                   senderId         = 4;

  protected long                   createdTimestamp = System.currentTimeMillis() / 1000;

  protected String                 message          = "message";

  protected List<Serializable>     entitiesToClean  = new ArrayList<>();

  @BeforeClass
  public static void beforeTest() {
    container = PortalContainer.getInstance();
    assertNotNull("Container shouldn't be null", container);
    assertTrue("Container should have been started", container.isStarted());
  }

  @Before
  public void beforeMethodTest() {
    RequestLifeCycle.begin(container);
  }

  @After
  public void afterMethodTest() {
    KudosDAO kudosDAO = getService(KudosDAO.class);

    RequestLifeCycle.end();
    RequestLifeCycle.begin(container);

    if (!entitiesToClean.isEmpty()) {
      for (Serializable entity : entitiesToClean) {
        if (entity instanceof KudosEntity) {
          kudosDAO.delete((KudosEntity) entity);
        } else if (entity instanceof Kudos) {
          Kudos kudos = (Kudos) entity;
          KudosEntity kudosEntity = kudosDAO.find(kudos.getTechnicalId());
          if (kudosEntity != null) {
            kudosDAO.delete(kudosEntity);
          }
        } else {
          throw new IllegalStateException("Entity not managed" + entity);
        }
      }
    }

    int kudosCount = kudosDAO.findAll().size();
    assertEquals("The previous test didn't cleaned kudos entities correctly, should add entities to clean into 'entitiesToClean' list.",
                 0,
                 kudosCount);

    RequestLifeCycle.end();
  }

  protected <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  protected Kudos newKudosDTO() {
    KudosEntity entity = newKudosInstance(parentEntityId, entityId, entityType, receiverId, senderId, createdTimestamp, message);
    return Utils.fromEntity(entity);
  }

  protected KudosEntity newKudos() {
    return newKudos(parentEntityId, entityId, entityType, receiverId, senderId, createdTimestamp, message);
  }

  protected KudosEntity newKudos(long parentEntityId,
                                 long entityId,
                                 int entityType,
                                 long receiverId,
                                 long senderId,
                                 long createdTimestamp,
                                 String message) {
    KudosDAO kudosDAO = getService(KudosDAO.class);

    KudosEntity kudosEntity = newKudosInstance(parentEntityId,
                                               entityId,
                                               entityType,
                                               receiverId,
                                               senderId,
                                               createdTimestamp,
                                               message);
    kudosEntity = kudosDAO.create(kudosEntity);
    entitiesToClean.add(kudosEntity);
    return kudosEntity;
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
    assertEquals(kudosEntity.getCreatedDate(), kudos.getTime().atZone(ZoneOffset.systemDefault()).toEpochSecond());
    assertEquals(String.valueOf(kudosEntity.getEntityId()), kudos.getEntityId());
    assertEquals(kudosEntity.getEntityType(), KudosEntityType.valueOf(kudos.getEntityType()).ordinal());
    assertEquals(kudosEntity.getId(), kudos.getTechnicalId());
    assertEquals(kudosEntity.getMessage(), kudos.getMessage());
    assertEquals(String.valueOf(kudosEntity.getParentEntityId()), kudos.getParentEntityId());
    assertEquals(String.valueOf(kudosEntity.getReceiverId()), kudos.getReceiverIdentityId());
    assertEquals(String.valueOf(kudosEntity.getSenderId()), kudos.getSenderIdentityId());
  }

  protected long getTime(int year, int month, int day) {
    return LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.systemDefault()).toEpochSecond();
  }

  protected long getCurrentTimeInSeconds() {
    return System.currentTimeMillis() / 1000 + 10;
  }

}
