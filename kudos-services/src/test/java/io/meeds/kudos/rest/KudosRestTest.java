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
package io.meeds.kudos.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.model.exception.KudosAlreadyLinkedException;
import io.meeds.kudos.service.KudosService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;
import lombok.SneakyThrows;

@SpringBootTest(classes = { KudosREST.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class KudosRestTest {

  private static final String REST_PATH     = "/kudos";      // NOSONAR

  private static final String SIMPLE_USER   = "simple";

  private static final String TEST_PASSWORD = "testPassword";

  static final ObjectMapper   OBJECT_MAPPER;

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER = JsonMapper.builder()
                              .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
                              .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                              .build();
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private KudosService          kudosService;

  @MockBean
  private IdentityManager       identityManager;

  private MockMvc               mockMvc;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void testGetAllKudos() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH).with(testAdminUser()));
    response.andExpect(status().isOk());
  }

  @Test
  public void testGetAllKudosByUser() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH).with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  public void testGetReceivedKudos() throws Exception {
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.DEFAULT);

    ResultActions response = mockMvc.perform(get(REST_PATH + "/1/received?returnSize=true&limit=10").with(testSimpleUser()));
    response.andExpect(status().isOk());
  }

  @Test
  public void testGetSentKudos() throws Exception {
    when(kudosService.getDefaultKudosPeriodType()).thenReturn(KudosPeriodType.DEFAULT);

    ResultActions response = mockMvc.perform(get(REST_PATH + "/1/sent?returnSize=true&limit=10").with(testSimpleUser()));
    response.andExpect(status().isOk());
  }

  @Test
  public void countKudosNoEntity() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/byEntity/sent/count").with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void countKudosNoEntityId() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/byEntity/sent/count?entityType=activity").with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void countKudosNoEntityType() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/byEntity/sent/count?entityId=1").with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void countKudos() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      ResultActions response = mockMvc.perform(get(REST_PATH +
          "/byEntity/sent/count?entityType=activity&entityId=1").with(testSimpleUser()));
      response.andExpect(status().isOk());
    }
  }

  @Test
  public void testGetKudosByActivityIdForbidden() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockConversationState()) {
      when(kudosService.getKudosByActivityId(anyLong(), any())).thenThrow(IllegalAccessException.class);

      ResultActions response = mockMvc.perform(get(REST_PATH + "/byActivity/1").with(testSimpleUser()));
      response.andExpect(status().isForbidden());
    }
  }

  @Test
  public void testGetKudosByActivityId() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockConversationState()) {
      ResultActions response = mockMvc.perform(get(REST_PATH + "/byActivity/1").with(testSimpleUser()));
      response.andExpect(status().isOk());
    }
  }

  @Test
  public void testGetKudosListOfActivityForbidden() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockConversationState()) {
      when(kudosService.getKudosListOfActivity(any(), any())).thenThrow(IllegalAccessException.class);

      ResultActions response = mockMvc.perform(get(REST_PATH + "/byActivity/1/all").with(testSimpleUser()));
      response.andExpect(status().isForbidden());
    }
  }

  @Test
  public void testGetKudosListOfActivity() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockConversationState()) {
      ResultActions response = mockMvc.perform(get(REST_PATH + "/byActivity/1/all").with(testSimpleUser()));
      response.andExpect(status().isOk());
    }
  }

  @Test
  public void testSendKudosNoEntityId() throws Exception {
    Kudos kudos = newKudos();
    kudos.setEntityId(null);
    ResultActions response = mockMvc.perform(post(REST_PATH).with(testSimpleUser())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(asJsonString(kudos)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void testSendKudosNoEntityType() throws Exception {
    Kudos kudos = newKudos();
    kudos.setEntityType(null);
    ResultActions response = mockMvc.perform(post(REST_PATH).with(testSimpleUser())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(asJsonString(kudos)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void testSendKudosNoReceiverId() throws Exception {
    Kudos kudos = newKudos();
    kudos.setReceiverId(null);
    ResultActions response = mockMvc.perform(post(REST_PATH).with(testSimpleUser())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(asJsonString(kudos)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void testSendKudosNoReceiverType() throws Exception {
    Kudos kudos = newKudos();
    kudos.setReceiverType(null);
    ResultActions response = mockMvc.perform(post(REST_PATH).with(testSimpleUser())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(asJsonString(kudos)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void testSendKudos() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      Kudos kudos = newKudos();
      ResultActions response = mockMvc.perform(post(REST_PATH).with(testSimpleUser())
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(asJsonString(kudos)));
      response.andExpect(status().isOk());
    }
  }

  @Test
  public void testDeleteKudosForbidden() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      doThrow(IllegalAccessException.class).when(kudosService).deleteKudosById(1, SIMPLE_USER);
      ResultActions response = mockMvc.perform(delete(REST_PATH + "/1").with(testSimpleUser()));
      response.andExpect(status().isForbidden());
    }
  }

  @Test
  public void testDeleteKudosNotFound() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      doThrow(ObjectNotFoundException.class).when(kudosService).deleteKudosById(1, SIMPLE_USER);
      ResultActions response = mockMvc.perform(delete(REST_PATH + "/1").with(testSimpleUser()));
      response.andExpect(status().isNotFound());
    }
  }

  @Test
  public void testDeleteKudosAlreadyLinked() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      doThrow(KudosAlreadyLinkedException.class).when(kudosService).deleteKudosById(1, SIMPLE_USER);
      ResultActions response = mockMvc.perform(delete(REST_PATH + "/1").with(testSimpleUser()));
      response.andExpect(status().isUnauthorized());
    }
  }

  @Test
  public void testDeleteKudos() throws Exception {
    try (MockedStatic<ConversationState> conversationStateMock = mockCurrentIdentityId()) {
      ResultActions response = mockMvc.perform(delete(REST_PATH + "/1").with(testSimpleUser()));
      response.andExpect(status().isOk());
    }
  }

  private Kudos newKudos() {
    Kudos kudos = new Kudos();
    kudos.setEntityType("activity");
    kudos.setEntityId("1");
    kudos.setReceiverType("user");
    kudos.setReceiverId("1");
    return kudos;
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

  private RequestPostProcessor testAdminUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("administrators"));
  }

  @SneakyThrows
  public static String asJsonString(final Object obj) {
    return OBJECT_MAPPER.writeValueAsString(obj);
  }

  @SneakyThrows
  public static MockedStatic<ConversationState> mockConversationState() {
    Identity identity = mock(Identity.class);
    ConversationState conversationState = mock(ConversationState.class);
    MockedStatic<ConversationState> conversationStateStatic = mockStatic(ConversationState.class);
    conversationStateStatic.when(ConversationState::getCurrent).thenReturn(conversationState);
    when(conversationState.getIdentity()).thenReturn(identity);
    return conversationStateStatic;
  }

  @SneakyThrows
  public MockedStatic<ConversationState> mockCurrentIdentityId() {
    MockedStatic<ConversationState> conversationStateStatic = mockConversationState();
    when(ConversationState.getCurrent().getIdentity().getUserId()).thenReturn(SIMPLE_USER);
    org.exoplatform.social.core.identity.model.Identity identity =
                                                                 mock(org.exoplatform.social.core.identity.model.Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    when(identityManager.getOrCreateUserIdentity(SIMPLE_USER)).thenReturn(identity);
    return conversationStateStatic;
  }

}
