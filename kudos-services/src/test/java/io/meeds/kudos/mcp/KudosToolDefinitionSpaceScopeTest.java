/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.meeds.mcp.server.constant.Origin;
import io.meeds.mcp.server.model.ActingIdentity;
import io.meeds.mcp.server.model.SimpleToolDefinition;
import io.meeds.mcp.server.model.SpaceScopeDeclaration;
import io.meeds.mcp.server.model.SpaceScopeRefusedException;
import io.meeds.mcp.server.plugin.SpaceScopeResolver;
import io.meeds.mcp.server.service.McpToolSpaceScopeService;
import io.meeds.mcp.server.util.McpToolUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pins the {@code space_scope} declarations the Kudos add-on ships: which of
 * its tools a call limited to one space may use, and on which arguments. The
 * {@code activity} resolver is the one mcp-server ships, looked up by name.
 */
class KudosToolDefinitionSpaceScopeTest {

  private static final String      DEFINITIONS      = "ai-tool-definitions.json";

  private static final String      ACTIVITY         = "activity";

  private static final String      SEND_ON_ACTIVITY = "send_kudos_on_activity";

  private static final String      LIST_ON_ACTIVITY = "list_kudos_on_activity";

  /**
   * An argument shaped like a reference to a place or an object: an id or
   * ids, in snake or camel case, a folder, a space.
   */
  private static final Pattern     REFERENCE_SHAPED = Pattern.compile("(^|_)ids?$|[a-z]Ids?$|(?i:folder|space)");

  private static final JsonMapper  MAPPER           = JsonMapper.builder().build();

  /**
   * Exactly the two expected tools declare a space scope, each of the right
   * kind: both name {@code activity_id} and the shipped {@code activity}
   * resolver, the send is a write and the list a read. Mutants: a
   * declaration dropped; the resolver renamed; the send marked read-only.
   */
  @Test
  void declaredToolsAreTheExpectedOnes() {
    Map<String, SimpleToolDefinition> declared = declaredTools();

    assertEquals(Set.of(SEND_ON_ACTIVITY, LIST_ON_ACTIVITY), declared.keySet());
    declared.forEach((name, tool) -> {
      boolean write = SEND_ON_ACTIVITY.equals(name);
      assertNotNull(tool.getAnnotations(), name);
      assertEquals(!write, McpToolSpaceScopeService.isReadTool(tool), name);
      SpaceScopeDeclaration scope = tool.getSpaceScope();
      assertTrue(scope.isWellFormedFor(write), name);
      assertTrue(scope.isObjectKind(), name);
      assertEquals("activity_id", scope.getObjectArg(), name);
      assertEquals(ACTIVITY, scope.getResolver(), name);
      assertFalse(scope.isHexId(), name);
      assertFalse(scope.isSpaceAdmin(), name);
    });
    assertTrue(declared.get(SEND_ON_ACTIVITY).isRequireApproval());
  }

  /**
   * The write accepts its activity and the message, nothing else; the read
   * accepts exactly its schema properties. Mutants: {@code message} removed
   * from the allowed arguments of the send; a property no schema holds
   * added.
   */
  @Test
  void acceptedArgumentsAreTheSchemaProperties() {
    declaredTools().forEach((name, tool) -> {
      Set<String> schema = new LinkedHashSet<>(properties(tool).propertyNames());
      Set<String> accepted = acceptedArguments(tool.getSpaceScope());
      assertEquals(schema, accepted, name);
    });
    assertEquals(Set.of("activity_id", "message"), acceptedArguments(declaredTools().get(SEND_ON_ACTIVITY).getSpaceScope()));
    assertEquals(List.of("message"), declaredTools().get(SEND_ON_ACTIVITY).getSpaceScope().getAllowedArgs());
  }

  /**
   * The write lets a call limited to a space pass nothing shaped like a
   * reference besides the post itself: no receiver, no space. Mutant:
   * {@code receiver_id} or {@code space_pretty_name} allowed on the send.
   */
  @Test
  void theWriteAcceptsNoReferenceBesidesItsPost() {
    SimpleToolDefinition send = declaredTools().get(SEND_ON_ACTIVITY);

    List<String> others = send.getSpaceScope().getAllowedArgs();
    others.forEach(argument -> assertFalse(REFERENCE_SHAPED.matcher(argument).find(), "accepts " + argument));
    properties(send).propertyNames()
                    .stream()
                    .filter(argument -> !"activity_id".equals(argument))
                    .forEach(argument -> assertFalse(REFERENCE_SHAPED.matcher(argument).find(),
                                                     "the schema holds " + argument));
    for (String reference : List.of("id", "ids", "receiver_id", "spaceId", "folderId", "space_pretty_name", "Space")) {
      assertTrue(REFERENCE_SHAPED.matcher(reference).find(), reference);
    }
    for (String plain : List.of("message", "limit", "period_type")) {
      assertFalse(REFERENCE_SHAPED.matcher(plain).find(), plain);
    }
  }

  /**
   * Every other kudos tool stays undeclared, so a call limited to a space
   * never runs it: the profile send, the cancel, the allowance of the asker
   * and the lists of any identity. Mutant: a space scope declared on any of
   * them.
   */
  @Test
  void theOtherKudosToolsStayUndeclared() {
    List<SimpleToolDefinition> shipped = McpToolUtils.parseToolDefinitions(definitionsUrl());

    assertEquals(Set.of("send_kudos", "cancel_kudos", "get_my_kudos_allowance", "list_received_kudos", "list_sent_kudos",
                        SEND_ON_ACTIVITY, LIST_ON_ACTIVITY),
                 shipped.stream().map(SimpleToolDefinition::getName).collect(Collectors.toSet()));
    shipped.stream()
           .filter(tool -> !Set.of(SEND_ON_ACTIVITY, LIST_ON_ACTIVITY).contains(tool.getName()))
           .forEach(tool -> assertNull(tool.getSpaceScope(), tool.getName()));
  }

  /**
   * Every public tool method has a definition and every definition a
   * method, and the properties of each schema are the method's parameter
   * names in snake case: a method with no definition is silently not a tool,
   * and a parameter the schema names differently binds to null. Mutant: the
   * {@code message} property renamed.
   */
  @Test
  void everyToolMethodHasItsDefinitionWithTheSameParameterNames() {
    Map<String, Method> methods = Arrays.stream(KudosMcpTool.class.getDeclaredMethods())
                                        .filter(method -> Modifier.isPublic(method.getModifiers()))
                                        .collect(Collectors.toMap(method -> snakeCase(method.getName()), method -> method));
    Map<String, SimpleToolDefinition> shipped = McpToolUtils.parseToolDefinitions(definitionsUrl())
                                                            .stream()
                                                            .collect(Collectors.toMap(SimpleToolDefinition::getName, tool -> tool));

    assertEquals(shipped.keySet(), methods.keySet());
    shipped.forEach((name, tool) -> {
      Set<String> parameters = Arrays.stream(methods.get(name).getParameters())
                                     .map(Parameter::getName)
                                     .map(KudosToolDefinitionSpaceScopeTest::snakeCase)
                                     .collect(Collectors.toSet());
      assertEquals(parameters, new LinkedHashSet<>(properties(tool).propertyNames()), name);
    });
  }

  /**
   * The shipped definitions, run through the server's check with a mocked
   * {@code activity} resolver: a post of the call's space passes for the
   * send under a proposal and for the list, a post of another space is
   * refused, a receiver argument is refused by name, and a send that isn't an
   * approved proposal is refused. Mutants: the resolver name changed; the
   * declaration dropped.
   */
  @Test
  @SuppressWarnings("unchecked")
  void shippedDeclarationsAreEnforcedUnderASpaceScope() {
    SpaceScopeResolver resolver = mock(SpaceScopeResolver.class);
    when(resolver.getName()).thenReturn(ACTIVITY);
    when(resolver.spaceOf("7")).thenReturn(12L);
    when(resolver.spaceOf("8")).thenReturn(99L);
    ObjectProvider<SpaceScopeResolver> provider = mock(ObjectProvider.class);
    when(provider.orderedStream()).thenAnswer(invocation -> Stream.of(resolver));
    McpToolSpaceScopeService service = new McpToolSpaceScopeService(provider);
    ActingIdentity read = ActingIdentity.agent("agent-kudos", "KUDOS_ASSISTANT", Origin.MENTION, 12L);
    ActingIdentity proposal = ActingIdentity.agent("agent-kudos", "KUDOS_ASSISTANT", Origin.PROPOSAL, 12L);
    Map<String, SimpleToolDefinition> shipped = McpToolUtils.parseToolDefinitions(definitionsUrl())
                                                            .stream()
                                                            .collect(Collectors.toMap(SimpleToolDefinition::getName, tool -> tool));
    SimpleToolDefinition send = shipped.get(SEND_ON_ACTIVITY);
    SimpleToolDefinition list = shipped.get(LIST_ON_ACTIVITY);

    String inSpace = "{\"activity_id\":\"7\",\"message\":\"Well done\"}";
    assertEquals(inSpace, service.enforce(SEND_ON_ACTIVITY, send, inSpace, proposal));
    String elsewhere = "{\"activity_id\":\"8\",\"message\":\"Well done\"}";
    assertThrows(SpaceScopeRefusedException.class, () -> service.enforce(SEND_ON_ACTIVITY, send, elsewhere, proposal));
    String withReceiver = "{\"activity_id\":\"7\",\"message\":\"Well done\",\"receiver_id\":\"someone\"}";
    SpaceScopeRefusedException refused = assertThrows(SpaceScopeRefusedException.class,
                                                      () -> service.enforce(SEND_ON_ACTIVITY, send, withReceiver, proposal));
    assertTrue(refused.getMessage().contains("receiver_id"), refused.getMessage());
    assertThrows(SpaceScopeRefusedException.class, () -> service.enforce(SEND_ON_ACTIVITY, send, inSpace, read));
    String otherSpaceName = "{\"activity_id\":\"7\",\"message\":\"Well done\",\"space_pretty_name\":\"other\"}";
    assertThrows(SpaceScopeRefusedException.class, () -> service.enforce(SEND_ON_ACTIVITY, send, otherSpaceName, proposal));

    String listInSpace = "{\"activity_id\":\"7\"}";
    assertEquals(listInSpace, service.enforce(LIST_ON_ACTIVITY, list, listInSpace, read));
    String listElsewhere = "{\"activity_id\":\"8\"}";
    assertThrows(SpaceScopeRefusedException.class, () -> service.enforce(LIST_ON_ACTIVITY, list, listElsewhere, read));
    String listComment = "{\"activity_id\":\"comment7\"}";
    assertThrows(SpaceScopeRefusedException.class, () -> service.enforce(LIST_ON_ACTIVITY, list, listComment, read));

    String profileKudos = "{\"receiver_type\":\"user\",\"receiver_id\":\"someone\",\"message\":\"Hi\"}";
    assertThrows(SpaceScopeRefusedException.class,
                 () -> service.enforce("send_kudos", shipped.get("send_kudos"), profileKudos, proposal));
    assertThrows(SpaceScopeRefusedException.class,
                 () -> service.enforce("cancel_kudos", shipped.get("cancel_kudos"), "{\"kudos_id\":3}", proposal));
  }

  /**
   * @param name a camel case name
   * @return the snake case name
   */
  private static String snakeCase(String name) {
    return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
  }

  /**
   * @param tool the tool
   * @return the properties of its input schema
   */
  private static JsonNode properties(SimpleToolDefinition tool) {
    return MAPPER.readTree(tool.getInputSchema()).path("properties");
  }

  /**
   * @param scope a space scope declaration
   * @return the arguments a limited call may carry
   */
  private static Set<String> acceptedArguments(SpaceScopeDeclaration scope) {
    Set<String> accepted = new LinkedHashSet<>();
    if (scope.getSpaceArg() != null) {
      accepted.add(scope.getSpaceArg());
    }
    if (scope.getObjectArg() != null) {
      accepted.add(scope.getObjectArg());
    }
    if (scope.getAllowedArgs() != null) {
      accepted.addAll(scope.getAllowedArgs());
    }
    if (scope.getFalseOnlyArgs() != null) {
      accepted.addAll(scope.getFalseOnlyArgs());
    }
    return accepted;
  }

  /**
   * @return the URL of the Kudos tool definitions this module ships
   */
  private URL definitionsUrl() {
    URL url = getClass().getClassLoader().getResource(DEFINITIONS);
    assertNotNull(url);
    return url;
  }

  /**
   * @return the shipped tools that declare a space scope, by name
   */
  private Map<String, SimpleToolDefinition> declaredTools() {
    return McpToolUtils.parseToolDefinitions(definitionsUrl())
                       .stream()
                       .filter(tool -> tool.getSpaceScope() != null)
                       .collect(Collectors.toMap(SimpleToolDefinition::getName, tool -> tool));
  }

}
