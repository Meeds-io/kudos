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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;
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

  private Identity                                              currentIdentity;

  private KudosMcpTool                                          kudosMcpTool;

  private MockedStatic<Utils>                                   utilsMock;

  @BeforeEach
  void setUp() {
    kudosService = mock(KudosService.class);
    spaceService = mock(SpaceService.class);
    identityManager = mock(IdentityManager.class);
    activityManager = mock(ActivityManager.class);
    currentIdentity = new Identity(USERNAME);
    utilsMock = mockStatic(Utils.class);
    kudosMcpTool = new KudosMcpTool(kudosService, spaceService, identityManager, activityManager) {
      @Override
      public Identity getCurrentUserAclIdentity() {
        return currentIdentity;
      }
    };
  }

  @AfterEach
  void tearDown() {
    utilsMock.close();
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

    KudosResultModel result = kudosMcpTool.sendKudos("user", RECEIVER, "Great work!");

    assertNotNull(result);
    assertEquals(USERNAME, result.getSender());
    assertEquals(RECEIVER, result.getReceiver());
    assertEquals("user", result.getReceiverType());
    verify(kudosService).createKudos(any(Kudos.class), eq(USERNAME));
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

    KudosResultModel result = kudosMcpTool.sendKudos("space", SPACE_PRETTY, "Great work!");

    assertNotNull(result);
    assertEquals(SPACE_PRETTY, result.getReceiver());
    assertEquals("space", result.getReceiverType());
    verify(kudosService).createKudos(any(Kudos.class), eq(USERNAME));
  }

  @Test
  void sendKudosBlankMessageFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "  "));
  }

  @Test
  void sendKudosBlankReceiverFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", " ", "Great work!"));
  }

  @Test
  void sendKudosInvalidTypeFails() {
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("group", RECEIVER, "Great work!"));
  }

  @Test
  void sendKudosToUnknownUserFails() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER)).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!"));
  }

  @Test
  void sendKudosToSelfFails() {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USERNAME))
        .thenReturn(socialIdentity(OWN_IDENTITY_ID, USERNAME, true));
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudos("user", USERNAME, "Great work!"));
  }

  @Test
  void sendKudosAllowanceExceededFails() throws Exception {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, RECEIVER))
        .thenReturn(socialIdentity(USER_IDENTITY_ID, RECEIVER, true));
    when(kudosService.createKudos(any(Kudos.class), eq(USERNAME)))
        .thenThrow(new IllegalAccessException("User having username'" + USERNAME + "' is not authorized to send more kudos"));
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudos("user", RECEIVER, "Great work!"));
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
    assertThrows(IllegalStateException.class, () -> kudosMcpTool.sendKudos("space", SPACE_PRETTY, "Great work!"));
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

}
