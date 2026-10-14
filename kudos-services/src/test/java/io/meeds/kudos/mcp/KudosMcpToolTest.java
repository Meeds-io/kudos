/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.kudos.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.kudos.mcp.model.KudosAllowanceModel;
import io.meeds.kudos.mcp.model.KudosResultModel;
import io.meeds.kudos.model.AccountSettings;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosList;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.model.exception.KudosAlreadyLinkedException;
import io.meeds.kudos.service.KudosService;
import io.meeds.kudos.service.utils.Utils;
import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;

class KudosMcpToolTest {

  private static final String                                   USERNAME         = "testuser1";

  private static final String                                   RECEIVER         = "receiver1";

  private static final String                                   USER_IDENTITY_ID = "42";

  private static final String                                   OWN_IDENTITY_ID  = "99";

  private static final String                                   SPACE_PRETTY     = "engineering";

  private static final String                                   SPACE_IDENTITY_ID = "77";

  private static final long                                     KUDOS_ID         = 5L;

  private KudosService                                          kudosService;

  private SpaceService                                          spaceService;

  private IdentityManager                                       identityManager;

  private ActivityManager                                       activityManager;

  private PermanentLinkService                                  permanentLinkService;

  private Identity                                              currentIdentity;

  private KudosMcpTool                                          kudosMcpTool;

  private MockedStatic<Utils>                                   utilsMock;

  private MockedStatic<CommonsUtils>                            commonsUtilsMock;

  @BeforeEach
  void setUp() {
    kudosService = mock(KudosService.class);
    spaceService = mock(SpaceService.class);
    identityManager = mock(IdentityManager.class);
    activityManager = mock(ActivityManager.class);
    currentIdentity = new Identity(USERNAME);
    permanentLinkService = mock(PermanentLinkService.class);
    utilsMock = mockStatic(Utils.class);
    commonsUtilsMock = mockStatic(CommonsUtils.class);
    commonsUtilsMock.when(CommonsUtils::getCurrentDomain).thenReturn("http://localhost:8080");
    kudosMcpTool = new KudosMcpTool(kudosService, spaceService, identityManager, activityManager, permanentLinkService) {
      @Override
      public Identity getCurrentUserAclIdentity() {
        return currentIdentity;
      }
    };
  }

  @AfterEach
  void tearDown() {
    utilsMock.close();
    commonsUtilsMock.close();
  }

  private org.exoplatform.social.core.identity.model.Identity socialIdentity(String id, String remoteId, boolean enabled) {
    org.exoplatform.social.core.identity.model.Identity identity =
        new org.exoplatform.social.core.identity.model.Identity(id);
    identity.setRemoteId(remoteId);
    identity.setEnable(enabled);
    identity.setDeleted(false);
    return identity;
  }

  private void stubCurrentUserIdentity() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USERNAME))
        .thenReturn(socialIdentity(OWN_IDENTITY_ID, USERNAME, true));
  }

  private Kudos sentKudos(String receiverId, String receiverType) {
    Kudos kudos = new Kudos();
    kudos.setTechnicalId(KUDOS_ID);
    kudos.setSenderId(USERNAME);
    kudos.setReceiverId(receiverId);
    kudos.setReceiverType(receiverType);
    kudos.setMessage("Great work!");
    kudos.setTimeInSeconds(1_700_000_000L);
    return kudos;
  }

  // --- send_kudos ----------------------------------------------------------

  @Test
  void sendKudosToUser() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sentKudos(RECEIVER, "user"));

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", null);

    assertNotNull(result);
    assertEquals(USERNAME, result.getSender());
    assertEquals(RECEIVER, result.getReceiver());
    assertEquals("user", result.getReceiverType());
    verify(kudosService).createKudos(any(Kudos.class), eq(USERNAME));
  }

  /**
   * The kudos comes back with the absolute link to the activity it generated,
   * resolved through the activity permanent link, so a caller can open it.
   */
  @Test
  void sendKudosReturnsTheLinkToItsActivity() throws Exception {
    Kudos sent = sentKudos(RECEIVER, "user");
    sent.setActivityId(321L);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sent);
    when(permanentLinkService.getLink(any(PermanentLinkObject.class))).thenReturn("/portal/dw/activity?id=321");

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", null);

    assertEquals("http://localhost:8080/portal/dw/activity?id=321", result.getUrl());
    ArgumentCaptor<PermanentLinkObject> linkCaptor = ArgumentCaptor.forClass(PermanentLinkObject.class);
    verify(permanentLinkService).getLink(linkCaptor.capture());
    assertEquals("activity", linkCaptor.getValue().getObjectType());
    assertEquals("321", linkCaptor.getValue().getObjectId());
  }

  /**
   * A kudos with no generated activity comes back without a link.
   */
  @Test
  void sendKudosWithoutActivityReturnsNoLink() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sentKudos(RECEIVER, "user"));

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", null);

    assertNull(result.getUrl());
    verifyNoInteractions(permanentLinkService);
  }

  @Test
  void sendKudosToSpace() throws Exception {
    Space space = new Space();
    space.setId("500");
    space.setPrettyName(SPACE_PRETTY);
    space.setDisplayName("Engineering");
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY)).thenReturn(space);
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, SPACE_PRETTY))
        .thenReturn(socialIdentity(SPACE_IDENTITY_ID, SPACE_PRETTY, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sentKudos(SPACE_PRETTY, "space"));

    KudosResultModel result = kudosMcpTool.sendKudos("space", SPACE_PRETTY, "Great work!", null);

    assertNotNull(result);
    assertEquals(SPACE_PRETTY, result.getReceiver());
    assertEquals("space", result.getReceiverType());
    verify(kudosService).createKudos(any(Kudos.class), eq(USERNAME));
  }

  @Test
  void sendKudosToUserInSpacePublishesInThatSpace() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    Space space = new Space();
    space.setId("500");
    space.setPrettyName(SPACE_PRETTY);
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY)).thenReturn(space);
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sentKudos(RECEIVER, "user"));

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", SPACE_PRETTY);

    assertNotNull(result);
    assertEquals("user", result.getReceiverType());
    assertEquals(RECEIVER, result.getReceiver());
    // The result names the space the kudos actually landed in, so the agent
    // does not have to infer it from the request it made
    assertEquals(SPACE_PRETTY, result.getSpacePrettyName());
    ArgumentCaptor<Kudos> captor = ArgumentCaptor.forClass(Kudos.class);
    verify(kudosService).createKudos(captor.capture(), eq(USERNAME));
    Kudos sent = captor.getValue();
    // The activity owner is the space as soon as spacePrettyName is set
    // (KudosSentActivityGeneratorListener), while the receiver stays the user
    assertEquals(SPACE_PRETTY, sent.getSpacePrettyName());
    assertEquals("user", sent.getReceiverType());
    assertEquals(RECEIVER, sent.getReceiverId());
  }

  @Test
  void sendKudosToUserWithoutSpaceStaysPersonal() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenReturn(sentKudos(RECEIVER, "user"));

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", "  ");

    assertNull(result.getSpacePrettyName());
    ArgumentCaptor<Kudos> captor = ArgumentCaptor.forClass(Kudos.class);
    verify(kudosService).createKudos(captor.capture(), eq(USERNAME));
    assertNull(captor.getValue().getSpacePrettyName());
  }

  @Test
  void sendKudosToUserInUnknownSpaceFails() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(spaceService.getSpaceByPrettyName("nowhere")).thenReturn(null);
    when(spaceService.getSpaceByGroupId(any())).thenReturn(null);

    assertThrows(IllegalArgumentException.class,
                 () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", "nowhere"));
  }

  @Test
  void sendKudosToUserInSpaceNotRedactorFails() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    Space space = new Space();
    space.setId("500");
    space.setPrettyName(SPACE_PRETTY);
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY)).thenReturn(space);
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User cannot redact on space"));

    IllegalStateException error = assertThrows(IllegalStateException.class,
                                               () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", SPACE_PRETTY));
    assertEquals("You are not allowed to post kudos in that space. You must be a space member who is allowed to post (a redactor).", error.getMessage());
    assertFalse(error.getMessage().contains("must be able to see it"));
  }

  @Test
  void sendKudosToSpaceWithAnotherSpaceFails() {
    Space receiverSpace = new Space();
    receiverSpace.setId("500");
    receiverSpace.setPrettyName(SPACE_PRETTY);
    Space otherSpace = new Space();
    otherSpace.setId("501");
    otherSpace.setPrettyName("marketing");
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY)).thenReturn(receiverSpace);
    when(spaceService.getSpaceByPrettyName("marketing")).thenReturn(otherSpace);

    assertThrows(IllegalArgumentException.class,
                 () -> kudosMcpTool.sendKudos("space", SPACE_PRETTY, "Great work!", "marketing"));
  }

  @Test
  void sendKudosBlankMessageFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "  ", null));
  }

  @Test
  void sendKudosBlankReceiverFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", " ", "Great work!", null));
  }

  @Test
  void sendKudosInvalidTypeFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("group", RECEIVER, "Great work!", null));
  }

  @Test
  void sendKudosToUnknownUserFails() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER)).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", null));
  }

  @Test
  void sendKudosToSelfFails() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USERNAME))
        .thenReturn(socialIdentity(OWN_IDENTITY_ID, USERNAME, true));
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudos("user", USERNAME, "Great work!", null));
  }

  @Test
  void sendKudosAllowanceExceededFails() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User having username'" + USERNAME + "' is not authorized to send more kudos"));
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!", null));
  }

  @Test
  void sendKudosNotRedactorFails() throws Exception {
    Space space = new Space();
    space.setId("500");
    space.setPrettyName(SPACE_PRETTY);
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY)).thenReturn(space);
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, SPACE_PRETTY))
        .thenReturn(socialIdentity(SPACE_IDENTITY_ID, SPACE_PRETTY, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User cannot redact on space"));
    IllegalStateException error = assertThrows(IllegalStateException.class,
                                               () -> kudosMcpTool.sendKudos("space", SPACE_PRETTY, "Great work!", null));
    assertEquals("You are not allowed to post kudos in that space. You must be a space member who is allowed to post (a redactor).", error.getMessage());
    assertFalse(error.getMessage().contains("must be able to see it"));
  }

  // --- cancel_kudos --------------------------------------------------------

  @Test
  void cancelKudos() throws Exception {
    String result = kudosMcpTool.cancelKudos(KUDOS_ID);
    assertTrue(result.contains(String.valueOf(KUDOS_ID)));
    verify(kudosService).deleteKudosById(KUDOS_ID, USERNAME);
  }

  @Test
  void cancelKudosInvalidIdFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.cancelKudos(null));
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.cancelKudos(0L));
  }

  @Test
  void cancelKudosNotSenderFails() throws Exception {
    doThrow(new IllegalAccessException("not allowed")).when(kudosService).deleteKudosById(KUDOS_ID, USERNAME);
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.cancelKudos(KUDOS_ID));
  }

  @Test
  void cancelKudosNotFoundFails() throws Exception {
    doThrow(new ObjectNotFoundException("missing")).when(kudosService).deleteKudosById(KUDOS_ID, USERNAME);
    assertThrows(ObjectNotFoundException.class, () -> kudosMcpTool.cancelKudos(KUDOS_ID));
  }

  @Test
  void cancelKudosAlreadyLinkedFails() throws Exception {
    doThrow(new KudosAlreadyLinkedException("linked")).when(kudosService).deleteKudosById(KUDOS_ID, USERNAME);
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.cancelKudos(KUDOS_ID));
  }

  // --- get_my_kudos_allowance ----------------------------------------------

  @Test
  void getMyKudosAllowance() {
    when(kudosService.getAccountSettings(USERNAME)).thenReturn(new AccountSettings(false, 2L));
    when(kudosService.getCurrentKudosPeriod()).thenReturn(new KudosPeriod(1_700_000_000L, 1_700_600_000L));
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.WEEK);

    KudosAllowanceModel allowance = kudosMcpTool.getMyKudosAllowance();

    assertNotNull(allowance);
    assertEquals(2L, allowance.getRemainingKudos());
    assertEquals("WEEK", allowance.getPeriodType());
    assertNotNull(allowance.getPeriodStart());
    assertNotNull(allowance.getPeriodEnd());
  }

  // --- list_received_kudos -------------------------------------------------

  @Test
  void listReceivedKudosDefaultsToSelf() {
    stubCurrentUserIdentity();
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.WEEK);
    when(kudosService.countKudosByPeriodAndReceiver(eq(99L), anyLong(), anyLong())).thenReturn(1L);
    when(kudosService.getKudosByPeriodAndReceiver(eq(99L), anyLong(), anyLong(), Mockito.anyInt()))
        .thenReturn(List.of(sentKudos(USERNAME, "user")));

    KudosList list = kudosMcpTool.listReceivedKudos(null, null, null);

    assertNotNull(list);
    assertEquals(1L, list.getSize());
    assertEquals(1, list.getKudos().size());
  }

  @Test
  void listReceivedKudosEmpty() {
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.MONTH);
    when(kudosService.countKudosByPeriodAndReceiver(eq(7L), anyLong(), anyLong())).thenReturn(0L);

    KudosList list = kudosMcpTool.listReceivedKudos(7L, null, null);

    assertNotNull(list);
    assertEquals(0L, list.getSize());
    assertTrue(list.getKudos().isEmpty());
  }

  @Test
  void listReceivedKudosInvalidPeriodFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.listReceivedKudos(7L, "DECADE", null));
  }

  // --- list_sent_kudos -----------------------------------------------------

  @Test
  void listSentKudos() {
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.WEEK);
    when(kudosService.countKudosByPeriodAndSender(eq(42L), anyLong(), anyLong())).thenReturn(2L);
    when(kudosService.getKudosByPeriodAndSender(eq(42L), anyLong(), anyLong(), Mockito.anyInt()))
        .thenReturn(List.of(sentKudos(RECEIVER, "user"), sentKudos("someone", "user")));

    KudosList list = kudosMcpTool.listSentKudos(42L, "WEEK", 5);

    assertNotNull(list);
    assertEquals(2L, list.getSize());
    assertEquals(2, list.getKudos().size());
  }

  @Test
  void listSentKudosDefaultsToSelf() {
    stubCurrentUserIdentity();
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.WEEK);
    when(kudosService.countKudosByPeriodAndSender(eq(99L), anyLong(), anyLong())).thenReturn(0L);

    KudosList list = kudosMcpTool.listSentKudos(null, null, null);

    assertNotNull(list);
    assertEquals(0L, list.getSize());
    assertTrue(list.getKudos().isEmpty());
  }

  // --- list_kudos_on_activity ----------------------------------------------

  @Test
  void listKudosOnActivity() throws Exception {
    when(kudosService.getKudosListOfActivity(eq("123"), any(Identity.class)))
        .thenReturn(List.of(sentKudos(RECEIVER, "user")));

    List<Kudos> kudos = kudosMcpTool.listKudosOnActivity("123");

    assertNotNull(kudos);
    assertEquals(1, kudos.size());
  }

  @Test
  void listKudosOnActivityBlankFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.listKudosOnActivity("  "));
  }

  @Test
  void listKudosOnActivityDeniedFails() throws Exception {
    when(kudosService.getKudosListOfActivity(eq("123"), any(Identity.class)))
        .thenThrow(new IllegalAccessException("denied"));
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.listKudosOnActivity("123"));
  }

  // --- send_kudos_on_activity ----------------------------------------------

  private static final String ACTIVITY_ID = "100";

  private static final String SPACE_ID    = "5";

  private static final String POSTER_ID   = "42";

  /**
   * @param id the activity id
   * @param posterIdentityId the identity id of its poster
   * @return a post of the space {@link #SPACE_ID}
   */
  private ExoSocialActivity spacePost(String id, String posterIdentityId) {
    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId(id);
    activity.setPosterId(posterIdentityId);
    activity.setSpaceId(SPACE_ID);
    activity.setType("DEFAULT_ACTIVITY");
    return activity;
  }

  /**
   * @param providerId the identity provider
   * @param id the identity id
   * @param remoteId the remote id
   * @return a social identity
   */
  private org.exoplatform.social.core.identity.model.Identity providerIdentity(String providerId, String id, String remoteId) {
    org.exoplatform.social.core.identity.model.Identity identity =
        new org.exoplatform.social.core.identity.model.Identity(providerId, remoteId);
    identity.setId(id);
    return identity;
  }

  /**
   * Stubs the space of the post, the post, its viewability for the current
   * user and the creation of the kudos by the service, which echoes the
   * receiver it is given.
   *
   * @param activity the post
   */
  private void stubPostInSpace(ExoSocialActivity activity) throws Exception {
    Space space = new Space();
    space.setId(SPACE_ID);
    space.setPrettyName(SPACE_PRETTY);
    space.setDisplayName("Engineering");
    when(spaceService.getSpaceById(SPACE_ID)).thenReturn(space);
    when(activityManager.getActivity(activity.getId())).thenReturn(activity);
    when(activityManager.isActivityViewable(eq(activity), any(Identity.class))).thenReturn(true);
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME))).thenAnswer(invocation -> {
      Kudos given = invocation.getArgument(0);
      return sentKudos(given.getReceiverId(), given.getReceiverType());
    });
  }

  /**
   * @return the kudos handed to the service by the call under test
   */
  private Kudos createdKudos() throws Exception {
    ArgumentCaptor<Kudos> captor = ArgumentCaptor.forClass(Kudos.class);
    verify(kudosService).createKudos(captor.capture(), eq(USERNAME));
    return captor.getValue();
  }

  /**
   * The receiver is the poster of the post, read from the activity and never
   * from an argument; the kudos is built as the UI builds an activity kudos:
   * type ACTIVITY, the activity as entity, no parent, the space of the
   * activity, the current user as sender. A poster who has since left the
   * space still receives it: no membership is read.
   */
  @Test
  void sendKudosOnActivityGoesToThePosterOfThePost() throws Exception {
    stubPostInSpace(spacePost(ACTIVITY_ID, POSTER_ID));
    when(identityManager.getIdentity(POSTER_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                             POSTER_ID,
                                                                             RECEIVER));

    KudosResultModel result = kudosMcpTool.sendKudosOnActivity(" " + ACTIVITY_ID + " ", " Well done ");

    Kudos kudos = createdKudos();
    assertEquals(RECEIVER, kudos.getReceiverId());
    assertEquals("user", kudos.getReceiverType());
    assertEquals(USERNAME, kudos.getSenderId());
    assertEquals("ACTIVITY", kudos.getEntityType());
    assertEquals(ACTIVITY_ID, kudos.getEntityId());
    assertEquals("", kudos.getParentEntityId());
    assertEquals(SPACE_PRETTY, kudos.getSpacePrettyName());
    assertEquals("Well done", kudos.getMessage());
    assertEquals(RECEIVER, result.getReceiver());
    assertEquals(SPACE_PRETTY, result.getSpacePrettyName());
    verify(spaceService, never()).isMember(any(Space.class), any(String.class));
    verify(spaceService, never()).isMember(any(String.class), any(String.class));
  }

  /**
   * A post that the space itself posted, as a space-owned activity, is
   * handled as the UI handles it: the receiver is that space.
   */
  @Test
  void sendKudosOnASpaceOwnedActivityGoesToTheSpace() throws Exception {
    stubPostInSpace(spacePost(ACTIVITY_ID, SPACE_IDENTITY_ID));
    when(identityManager.getIdentity(SPACE_IDENTITY_ID)).thenReturn(providerIdentity(SpaceIdentityProvider.NAME,
                                                                                     SPACE_IDENTITY_ID,
                                                                                     SPACE_PRETTY));

    kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Great space");

    Kudos kudos = createdKudos();
    assertEquals(SPACE_PRETTY, kudos.getReceiverId());
    assertEquals("space", kudos.getReceiverType());
    assertEquals(SPACE_PRETTY, kudos.getSpacePrettyName());
  }

  /**
   * When the post is itself a kudos activity, the receiver of that kudos is
   * credited, not the poster of the activity.
   */
  @Test
  void sendKudosOnAKudosActivityCreditsItsReceiver() throws Exception {
    ExoSocialActivity activity = spacePost(ACTIVITY_ID, POSTER_ID);
    activity.setType("exokudos:activity");
    stubPostInSpace(activity);
    when(identityManager.getIdentity(POSTER_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                             POSTER_ID,
                                                                             "theposter"));
    when(kudosService.getKudosByActivityId(eq(100L), any(Identity.class))).thenReturn(sentKudos("original", "user"));

    kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Well deserved");

    Kudos kudos = createdKudos();
    assertEquals("original", kudos.getReceiverId());
    assertEquals("user", kudos.getReceiverType());
    assertEquals("ACTIVITY", kudos.getEntityType());
  }

  /**
   * A kudos activity whose kudos can't be found is refused rather than
   * falling back to its poster.
   */
  @Test
  void sendKudosOnAKudosActivityWithoutKudosIsRefused() throws Exception {
    ExoSocialActivity activity = spacePost(ACTIVITY_ID, POSTER_ID);
    activity.setType("exokudos:activity");
    stubPostInSpace(activity);
    when(kudosService.getKudosByActivityId(eq(100L), any(Identity.class))).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Well deserved"));
    verify(kudosService, never()).createKudos(any(Kudos.class), any(String.class));
  }

  /**
   * The worker's own post is handed to the service with the worker as
   * receiver, and the service's refusal is reported: the tool doesn't decide
   * it.
   */
  @Test
  void sendKudosOnOwnPostIsRefusedByTheService() throws Exception {
    stubPostInSpace(spacePost(ACTIVITY_ID, OWN_IDENTITY_ID));
    when(identityManager.getIdentity(OWN_IDENTITY_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                                   OWN_IDENTITY_ID,
                                                                                   USERNAME));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User with username 'testuser1' is not authorized to send kudos to himseld!"));

    IllegalStateException error = assertThrows(IllegalStateException.class,
                                               () -> kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Me"));

    assertEquals("You cannot send a kudos to yourself.", error.getMessage());
    assertEquals(USERNAME, createdKudos().getReceiverId());
  }

  /**
   * On a post, the space right is that of seeing the space: the refusal is
   * worded so, not as a redactor requirement.
   */
  @Test
  void sendKudosOnActivityNotAllowedInSpaceMentionsViewingNotRedactor() throws Exception {
    stubPostInSpace(spacePost(ACTIVITY_ID, POSTER_ID));
    when(identityManager.getIdentity(POSTER_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                             POSTER_ID,
                                                                             RECEIVER));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User cannot redact on space"));

    IllegalStateException error = assertThrows(IllegalStateException.class,
                                               () -> kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Hi"));

    assertEquals("You are not allowed to send kudos in that space: you must be able to see it.", error.getMessage());
    assertFalse(error.getMessage().contains("redactor"));
  }

  /**
   * A missing activity, a comment and a post of no space stream are refused
   * before anything is sent.
   */
  @Test
  void sendKudosOnAMissingActivityACommentOrAPostOfNoSpaceIsRefused() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("999", "Hi"));

    ExoSocialActivity userStreamPost = spacePost("300", POSTER_ID);
    userStreamPost.setSpaceId(null);
    when(activityManager.getActivity("300")).thenReturn(userStreamPost);
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("300", "Hi"));

    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "  "));
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity(" ", "Hi"));
    verify(kudosService, never()).createKudos(any(Kudos.class), any(String.class));
  }

  /**
   * A comment is refused on its own account: it is in a space, viewable and
   * with a known poster, so only the rule "a post, not a comment" stops it,
   * whether it is flagged a comment or has a parent.
   */
  @Test
  void sendKudosOnACommentIsRefusedWhateverItsSpace() throws Exception {
    ExoSocialActivity post = spacePost(ACTIVITY_ID, POSTER_ID);
    stubPostInSpace(post);
    when(identityManager.getIdentity(POSTER_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                             POSTER_ID,
                                                                             RECEIVER));
    ExoSocialActivity comment = spacePost("200", POSTER_ID);
    comment.isComment(true);
    ExoSocialActivity reply = spacePost("201", POSTER_ID);
    reply.setParentId(ACTIVITY_ID);
    for (ExoSocialActivity target : List.of(comment, reply)) {
      when(activityManager.getActivity(target.getId())).thenReturn(target);
      when(activityManager.isActivityViewable(eq(target), any(Identity.class))).thenReturn(true);
    }

    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("200", "Hi"));
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("201", "Hi"));
    verify(kudosService, never()).createKudos(any(Kudos.class), any(String.class));
  }

  /**
   * An id that isn't a plain number is refused as such, even when the
   * activity lookup would resolve it to a post of the space.
   */
  @Test
  void sendKudosOnANonNumericIdIsRefused() throws Exception {
    stubPostInSpace(spacePost(ACTIVITY_ID, POSTER_ID));
    when(identityManager.getIdentity(POSTER_ID)).thenReturn(providerIdentity(OrganizationIdentityProvider.NAME,
                                                                             POSTER_ID,
                                                                             RECEIVER));
    for (String id : List.of("100abc", "comment202")) {
      ExoSocialActivity odd = spacePost(id, POSTER_ID);
      when(activityManager.getActivity(id)).thenReturn(odd);
      when(activityManager.isActivityViewable(eq(odd), any(Identity.class))).thenReturn(true);
    }

    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("100abc", "Hi"));
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudosOnActivity("comment202", "Hi"));
    verify(kudosService, never()).createKudos(any(Kudos.class), any(String.class));
  }

  /**
   * A post the current user can't view is refused before anything is sent.
   */
  @Test
  void sendKudosOnAPostThatCannotBeViewedIsRefused() throws Exception {
    ExoSocialActivity activity = spacePost(ACTIVITY_ID, POSTER_ID);
    stubPostInSpace(activity);
    when(activityManager.isActivityViewable(eq(activity), any(Identity.class))).thenReturn(false);

    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudosOnActivity(ACTIVITY_ID, "Hi"));
    verify(kudosService, never()).createKudos(any(Kudos.class), any(String.class));
  }

}
