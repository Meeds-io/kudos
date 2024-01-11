package org.exoplatform.kudos.test;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.kudos.dao.KudosDAO;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.service.utils.Utils;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/kudos-test-configuration.xml"),
})
public abstract class BaseKudosTest extends AbstractKernelTest {

  protected static final String    DEFAULT_PORTAL   = "meeds";

  protected KudosEntityType        kudosEntityType  = KudosEntityType.USER_TIPTIP;

  protected int                    entityType       = kudosEntityType.ordinal();

  protected long                   entityId         = 1;

  protected long                   parentEntityId   = 2;

  protected long                   receiverId       = 3;

  protected long                   senderId         = 4;

  protected long                   createdTimestamp = System.currentTimeMillis() / 1000;

  protected String                 message          = "message";

  protected PortalContainer     container;

  @Before
  @Override
  public void setUp() throws Exception {
    container = getContainer();
    assertNotNull("Container shouldn't be null", container);
    assertTrue("Container should have been started", container.isStarted());
    begin();
    super.setUp();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    KudosDAO kudosDAO = getService(KudosDAO.class);

    restartTransaction();

    kudosDAO.deleteAll();

    int kudosCount = kudosDAO.findAll().size();
    assertEquals("The previous test didn't cleaned kudos entities correctly, should add entities to clean into 'entitiesToClean' list.",
                 0,
                 kudosCount);

    end();
    super.tearDown();
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
    KudosDAO kudosDAO = getService(KudosDAO.class);

    KudosEntity kudosEntity = newKudosInstance(parentEntityId,
                                               entityId,
                                               entityType,
                                               receiverId,
                                               senderId,
                                               createdTimestamp,
                                               message);
    return kudosDAO.create(kudosEntity);
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
