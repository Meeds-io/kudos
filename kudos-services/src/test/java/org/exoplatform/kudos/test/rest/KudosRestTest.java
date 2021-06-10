package org.exoplatform.kudos.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.*;

import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosList;
import org.exoplatform.kudos.rest.KudosREST;
import org.exoplatform.kudos.test.BaseKudosRestTest;
import org.exoplatform.services.rest.impl.ContainerResponse;

@SuppressWarnings("unchecked")
public class KudosRestTest extends BaseKudosRestTest {

  @Before
  public void setUp() throws Exception {
    beforeBeginTest();
    resourceTest.addResource(KudosREST.class, null);
  }

  @After
  public void tearDown() throws Exception {
    afterEndTest();
    resourceTest.removeResource(KudosREST.class);
  }

  @Test
  public void testGetAllKudos() throws Exception {
    resourceTest.startSessionAs("root4");
    begin();
    try {
      ContainerResponse response = resourceTest.service("GET", resourceTest.getURLResource(""), "", null, null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      List<Kudos> kudosList = (List<Kudos>) response.getEntity();
      assertEquals(0, kudosList.size());

      newKudos();

      response = resourceTest.service("GET", resourceTest.getURLResource(""), "", null, null);
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
    String url = resourceTest.getURLResource(receiverId + "/received?returnSize=true&limit=10");

    resourceTest.startSessionAs("root4");
    ContainerResponse response = resourceTest.service("GET",
                                                      url,
                                                      "",
                                                      null,
                                                      null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    KudosList kudosList = (KudosList) response.getEntity();
    assertEquals(0, kudosList.getSize());

    newKudos();

    response = resourceTest.service("GET",
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
    String url = resourceTest.getURLResource(senderId + "/sent?returnSize=true&limit=10");

    resourceTest.startSessionAs("root4");
    ContainerResponse response = resourceTest.service("GET",
                                                      url,
                                                      "",
                                                      null,
                                                      null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    KudosList kudosList = (KudosList) response.getEntity();
    assertEquals(0, kudosList.getSize());

    newKudos();

    response = resourceTest.service("GET",
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
  public void testCountSentKudosByEntityAndUser() throws Exception {
    resourceTest.startSessionAs("root4");

    String url = resourceTest.getURLResource("byEntity/sent/count?entityType=" + kudosEntityType + "&entityId=" + entityId);
    ContainerResponse response = resourceTest.service("GET",
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
    
    response = resourceTest.service("GET",
                                    url,
                                    "",
                                    null,
                                    null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    count = (String) response.getEntity();
    assertNotNull(count);
    assertEquals("1", count);

    resourceTest.startSessionAs("root3");

    response = resourceTest.service("GET",
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

}
