package org.exoplatform.kudos.test.rest;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.listener.NewKudosSentActivityGeneratorListener;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.model.KudosList;
import org.exoplatform.kudos.rest.KudosREST;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.kudos.service.utils.Utils;
import org.exoplatform.kudos.test.BaseKudosRestTest;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;

@SuppressWarnings("unchecked")
public class KudosRestTest extends BaseKudosRestTest {

  @Before
  public void setUp() throws Exception {
    super.setUp();
    addResource(KudosREST.class, null);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(KudosREST.class);
  }

  @Override
  public String getURLResource(String resourceURL) {
    return "/kudos/api/kudos/" + resourceURL;
  }

  @Test
  public void testGetAllKudos() throws Exception {
    startSessionAs("root4");
    begin();
    try {
      ContainerResponse response = service("GET", getURLResource(""), "", null, null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      List<Kudos> kudosList = (List<Kudos>) response.getEntity();
      assertEquals(0, kudosList.size());

      newKudos();

      response = service("GET", getURLResource(""), "", null, null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      kudosList = (List<Kudos>) response.getEntity();
      assertEquals(1, kudosList.size());
      Kudos retrievedKudos = kudosList.get(0);
      assertNotNull(retrievedKudos);
    } finally {
      end();
    }
  }

  @Test
  public void testGetReceivedKudos() throws Exception {
    String url = getURLResource(receiverId + "/received?returnSize=true&limit=10");

    startSessionAs("root4");
    ContainerResponse response = service("GET",
                                         url,
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    KudosList kudosList = (KudosList) response.getEntity();
    assertEquals(0, kudosList.getSize());

    newKudos();

    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    kudosList = (KudosList) response.getEntity();
    assertEquals(1, kudosList.getSize());
    Kudos retrievedKudos = kudosList.getKudos().get(0);
    assertNotNull(retrievedKudos);
  }

  @Test
  public void testGetSentKudos() throws Exception {
    String url = getURLResource(senderId + "/sent?returnSize=true&limit=10");

    startSessionAs("root4");
    ContainerResponse response = service("GET",
                                         url,
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    KudosList kudosList = (KudosList) response.getEntity();
    assertEquals(0, kudosList.getSize());

    newKudos();

    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    kudosList = (KudosList) response.getEntity();
    assertEquals(1, kudosList.getSize());
    Kudos retrievedKudos = kudosList.getKudos().get(0);
    assertNotNull(retrievedKudos);
  }

  @Test
  public void testGetKudosByActivityId() throws Exception {
    startSessionAs("root4");
    KudosService kudosService = getService(KudosService.class);

    KudosEntity kudosEntity = newKudosInstance();
    Kudos kudos = kudosService.createKudos(Utils.fromEntity(kudosEntity, DEFAULT_PORTAL), "root4");
    long activityId = 5;

    kudosService.updateKudosGeneratedActivityId(kudos.getTechnicalId(), activityId);
    kudos = kudosService.getKudosByActivityId(activityId);

    assertEquals(activityId, kudos.getActivityId());

    String url = getURLResource("byActivity/" + activityId);
    ContainerResponse response = service("GET",
                                         url,
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    Kudos kudosByActivity = (Kudos) response.getEntity();
    assertNotNull(kudosByActivity);
    assertTrue(kudosByActivity.getActivityId() > 0);
    assertEquals(kudos.getTechnicalId(), kudosByActivity.getTechnicalId());

    url = getURLResource("byActivity/200");
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    kudosByActivity = (Kudos) response.getEntity();
    assertNull(kudosByActivity);

    startSessionAs("root3");
    url = getURLResource("byActivity/" + activityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    kudosByActivity = (Kudos) response.getEntity();
    assertNotNull(kudosByActivity);
    assertTrue(kudosByActivity.getActivityId() > 0);
    assertEquals(kudos.getTechnicalId(), kudosByActivity.getTechnicalId());

    startSessionAs("root2");
    url = getURLResource("byActivity/" + activityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    kudosByActivity = (Kudos) response.getEntity();
    assertNull(kudosByActivity);

    ActivityManager activityManager = getService(ActivityManager.class);
    new NewKudosSentActivityGeneratorListener(activityManager, null).onEvent(new Event<KudosService, Kudos>(null,
                                                                                                            kudosService,
                                                                                                            kudos));
    List<Kudos> kudosList = kudosService.getKudosByEntity(kudos.getEntityType(), kudos.getEntityId(), 1);
    assertEquals(1, kudosList.size());
    kudos = kudosList.get(0);

    activityId = kudos.getActivityId();

    url = getURLResource("byActivity/" + activityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    startSessionAs("root4");
    url = getURLResource("byActivity/" + activityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    startSessionAs("root3");
    url = getURLResource("byActivity/" + activityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testGetKudosListOfActivity() throws Exception {
    String senderUsername = "root4";

    startSessionAs(senderUsername);
    KudosService kudosService = getService(KudosService.class);

    KudosEntity kudosEntity = newKudosInstance();
    kudosEntity.setEntityType(KudosEntityType.SPACE_PROFILE.ordinal());
    Kudos parentKudos = kudosService.createKudos(Utils.fromEntity(kudosEntity, DEFAULT_PORTAL), senderUsername);

    ActivityManager activityManager = getService(ActivityManager.class);
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setUserId(senderUsername);
    activityManager.saveActivityNoReturn(activity);
    String activityId = activity.getId();
    parentKudos.setActivityId(Long.parseLong(activityId));
    kudosService.updateKudosGeneratedActivityId(parentKudos.getTechnicalId(), Long.parseLong(activityId));

    KudosEntity childKudosEntity = newKudosInstance();
    childKudosEntity.setEntityType(KudosEntityType.ACTIVITY.ordinal());
    childKudosEntity.setEntityId(250l);
    childKudosEntity.setParentEntityId(parentKudos.getActivityId());
    Kudos childKudos = kudosService.createKudos(Utils.fromEntity(childKudosEntity, DEFAULT_PORTAL), senderUsername);

    getKudosListOfActivity("%20", senderUsername, 400);
    getKudosListOfActivity(activityId, "root3", 404);

    List<Kudos> kudosList = getKudosListOfActivity("66699963", senderUsername, 200);
    assertNotNull(kudosList);
    assertTrue(kudosList.isEmpty());

    kudosList = getKudosListOfActivity(activityId, senderUsername, 200);
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
    Kudos subCommentKudos = kudosService.createKudos(Utils.fromEntity(subCommentKudosEntity, DEFAULT_PORTAL), senderUsername);

    kudosList = getKudosListOfActivity(activityId, senderUsername, 200);

    assertNotNull(kudosList);
    assertEquals(3, kudosList.size());
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == parentKudos.getTechnicalId()));
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == childKudos.getTechnicalId()));
    assertTrue(kudosList.stream().anyMatch(kudos -> kudos.getTechnicalId() == subCommentKudos.getTechnicalId()));
  }

  @Test
  public void testCountSentKudosByEntityAndUser() throws Exception {
    startSessionAs("root4");

    String url = getURLResource("byEntity/sent/count?entityType=" + kudosEntityType + "&entityId=" + entityId);
    ContainerResponse response = service("GET",
                                         url,
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    String count = (String) response.getEntity();
    assertNotNull(count);
    assertEquals("0", count);

    newKudos();

    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    count = (String) response.getEntity();
    assertNotNull(count);
    assertEquals("1", count);

    startSessionAs("root3");

    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    count = (String) response.getEntity();
    assertNotNull(count);
    assertEquals("0", count);
  }

  @Test
  public void testSendKudos() throws Exception {
    startSessionAs("root4");

    String url = getURLResource("");
    String input = "{\"entityId\":\"" + entityId + "\", \"entityType\":\"" + kudosEntityType.name() + "\", \"receiverId\":\"root"
        + receiverId
        + "\", \"receiverType\":\"" + OrganizationIdentityProvider.NAME + "\", \"parentEntityId\":\"" + parentEntityId
        + "\", \"message\":\"" + message + "\"}";
    ContainerResponse response = getResponse("POST",
                                             url,
                                             input);

    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());

    Kudos kudos = (Kudos) response.getEntity();
    assertEquals(String.valueOf(entityId), kudos.getEntityId());
    assertEquals(String.valueOf(receiverId), kudos.getReceiverIdentityId());
    assertEquals(String.valueOf(parentEntityId), kudos.getParentEntityId());
    assertEquals(kudosEntityType.name(), kudos.getEntityType());
    assertEquals(message, kudos.getMessage());

    url = getURLResource("byEntity/sent/count?entityType=" + kudosEntityType + "&entityId=" + entityId);
    response = service("GET",
                       url,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    String count = (String) response.getEntity();
    assertNotNull(count);
    assertEquals("1", count);
  }

  private List<Kudos> getKudosListOfActivity(String activityId, String username, int expectedStatus) throws Exception {
    startSessionAs(username);
    String url = getURLResource("byActivity/" + activityId + "/all");
    ContainerResponse response = service("GET",
                                         url,
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(response.getEntity() == null ? "Unexpected status: " + response.getStatus() : response.getEntity().toString(),
                 expectedStatus,
                 response.getStatus());
    List<Kudos> kudosList = null;
    if (expectedStatus == 200) {
      kudosList = (List<Kudos>) response.getEntity();
    }
    return kudosList;
  }

}
