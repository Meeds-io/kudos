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

import static io.meeds.kudos.service.utils.Utils.getCurrentUserId;
import static io.meeds.kudos.service.utils.Utils.timeFromSeconds;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.utils.MentionUtils;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosList;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.model.exception.KudosAlreadyLinkedException;
import io.meeds.kudos.service.KudosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("kudos")
@Tag(name = "/kudos/rest/kudos", description = "Manages Kudos") // NOSONAR
public class KudosREST {

  @Autowired
  private KudosService    kudosService;

  @Autowired
  private IdentityManager identityManager;

  @GetMapping
  @Secured("administrators")
  @Operation(
             summary = "Get Kudos list created in a period contained a selected date in seconds",
             method = "GET",
             description = "Get Kudos list created in a period contained a selected date in seconds and returns list of Kudos")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public List<Kudos> getKudosByPeriodOfDate(
                                            @Parameter(description = "Timestamp in seconds of date in the middle of selected period. If not defined, current time will be used.")
                                            @RequestParam(name = "dateInSeconds", required = false, defaultValue = "0")
                                            long dateInSeconds,
                                            @Parameter(description = "Limit of results to return")
                                            @RequestParam(name = "limit", required = false, defaultValue = "10")
                                            int limit) {
    if (dateInSeconds <= 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }
    List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriodOfDate(dateInSeconds, getLimit(limit));
    translateRoleMentions(allKudosByPeriod.toArray(new Kudos[0]));
    return allKudosByPeriod;
  }

  @GetMapping("byEntity")
  @Secured("users")
  @Operation(summary = "Get Kudos list by entity type and id", method = "GET",
             description = "Get Kudos list by entity type and id and returns list of Kudos")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public List<Kudos> getEntityKudos(
                                    @Parameter(description = "kudos entity type (for example activity, comment...)",
                                               required = true)
                                    @RequestParam("entityType")
                                    String entityType,
                                    @Parameter(description = "kudos entity id", required = true)
                                    @RequestParam("entityId")
                                    String entityId,
                                    @Parameter(description = "Limit of results to return")
                                    @RequestParam(name = "limit", required = false, defaultValue = "10")
                                    int limit) {
    List<Kudos> allKudosByEntity = kudosService.getKudosByEntity(entityType, entityId, getLimit(limit));
    translateRoleMentions(allKudosByEntity.toArray(new Kudos[0]));
    return allKudosByEntity;
  }

  @GetMapping("byActivity/{activityId}")
  @Secured("users")
  @Operation(summary = "Get Kudos by its generated comment or activity id", method = "GET",
             description = "Get Kudos by its generated comment or activity id")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "404", description = "Entity Not found"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Kudos getKudosByActivityId(
                                    @Parameter(description = "kudos activity or comment identifier", required = true)
                                    @PathVariable("activityId")
                                    String activityId) {
    try {
      Kudos kudos = kudosService.getKudosByActivityId(getActivityId(activityId), getCurrentIdentity());
      translateRoleMentions(kudos);
      return kudos;
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("byActivity/{activityId}/all")
  @Secured("users")
  @Operation(
             summary = "Get Kudos List attached to a parent activity, whether the activity itself or in a comment",
             method = "GET",
             description = "Get Kudos List attached to a parent activity, whether the activity itself or in a comment")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "404", description = "Entity Not found"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public List<Kudos> getKudosListOfActivity(
                                            @Parameter(description = "kudos parent activity identifier", required = true)
                                            @PathVariable("activityId")
                                            String activityId) {
    org.exoplatform.services.security.Identity currentUser = getCurrentIdentity();
    try {
      List<Kudos> kudosList = kudosService.getKudosListOfActivity(activityId, currentUser);
      translateRoleMentions(kudosList.toArray(new Kudos[0]));
      return kudosList;
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("byEntity/sent/count")
  @Secured("users")
  @Operation(
             summary = "Get Kudos count by entity and current user as sender",
             method = "GET",
             description = "Get Kudos count by entity and current user as sender")
  @ApiResponses(
                value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public long countKudosByEntityAndSender(
                                          @Parameter(
                                                     description = "kudos entity type (for example activity, comment...)",
                                                     required = true)
                                          @RequestParam("entityType")
                                          String entityType,
                                          @Parameter(description = "kudos entity id", required = true)
                                          @RequestParam("entityId")
                                          String entityId) {
    return kudosService.countKudosByEntityAndSender(entityType, entityId, getCurrentIdentityId());
  }

  @GetMapping("byDates")
  @Secured("administrators")
  @Operation(
             summary = "Get Kudos list created between start and end dates in seconds",
             method = "GET",
             description = "Get Kudos list created between start and end dates in seconds")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public List<Kudos> getKudosByDates(
                                     @RequestParam("startDateInSeconds")
                                     long startDateInSeconds,
                                     @RequestParam("endDateInSeconds")
                                     long endDateInSeconds,
                                     @Parameter(description = "Limit of results to return")
                                     @RequestParam(name = "limit", required = false, defaultValue = "10")
                                     int limit) {
    List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriod(startDateInSeconds, endDateInSeconds, getLimit(limit));
    translateRoleMentions(allKudosByPeriod.toArray(new Kudos[0]));
    return allKudosByPeriod;
  }

  @GetMapping("{identityId}/received")
  @Secured("users")
  @Operation(
             summary = "Retrieve the list of received Kudos by a user or space in a selected period",
             method = "GET",
             description = "Retrieve the list of received Kudos by a user or space in a selected period")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid query input"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public KudosList getReceivedKudosByPeriod(
                                            @Parameter(description = "User or space identity technical id", required = true)
                                            @PathVariable("identityId")
                                            long identityId,
                                            @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds",
                                                       required = true)
                                            @RequestParam(name = "dateInSeconds", required = false, defaultValue = "0")
                                            long dateInSeconds,
                                            @Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period",
                                                       required = false)
                                            @RequestParam(name = "periodType", required = false)
                                            String periodType,
                                            @Parameter(description = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved",
                                                       required = false)
                                            @RequestParam(name = "limit", required = false, defaultValue = "10")
                                            int limit,
                                            @Parameter(description = "Whether return size of received kudos, default = false")
                                            @RequestParam(name = "returnSize", required = false, defaultValue = "false")
                                            boolean returnSize) {
    KudosPeriodType kudosPeriodType = getKudosPeriodType(periodType);
    KudosPeriod period = kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
    KudosList kudosList = new KudosList();
    if (returnSize) {
      long size = kudosService.countKudosByPeriodAndReceiver(identityId,
                                                             period.getStartDateInSeconds(),
                                                             period.getEndDateInSeconds());
      kudosList.setSize(size);
      if (size == 0 || limit == 0) {
        return kudosList;
      }
    }
    List<Kudos> kudos = kudosService.getKudosByPeriodAndReceiver(identityId,
                                                                 period.getStartDateInSeconds(),
                                                                 period.getEndDateInSeconds(),
                                                                 getLimit(limit));
    translateRoleMentions(kudos.toArray(new Kudos[0]));
    kudosList.setKudos(kudos);
    return kudosList;
  }

  @GetMapping("{identityId}/sent")
  @Secured("users")
  @Operation(
             summary = "Retrieve the list of sent Kudos for a user in a selected period",
             method = "GET",
             description = "Retrieve the list of sent Kudos for a user in a selected period")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public KudosList getSentKudosByPeriod(
                                        @Parameter(description = "User or space identity technical id", required = true)
                                        @PathVariable("identityId")
                                        long identityId,
                                        @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds")
                                        @RequestParam(name = "dateInSeconds", required = false, defaultValue = "0")
                                        long dateInSeconds,
                                        @Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period")
                                        @RequestParam(name = "periodType", required = false)
                                        String periodType,
                                        @Parameter(description = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved")
                                        @RequestParam(name = "limit", required = false, defaultValue = "10")
                                        int limit,
                                        @Parameter(description = "Whether return size of sent kudos, default = false")
                                        @RequestParam(name = "returnSize", required = false, defaultValue = "false")
                                        boolean returnSize) {
    KudosPeriodType kudosPeriodType = getKudosPeriodType(periodType);
    KudosPeriod period = kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
    KudosList kudosList = new KudosList();
    if (returnSize) {
      long size = kudosService.countKudosByPeriodAndSender(identityId,
                                                           period.getStartDateInSeconds(),
                                                           period.getEndDateInSeconds());
      kudosList.setSize(size);
      if (size == 0 || limit == 0) {
        return kudosList;
      }
    }

    List<Kudos> kudos = kudosService.getKudosByPeriodAndSender(identityId,
                                                               period.getStartDateInSeconds(),
                                                               period.getEndDateInSeconds(),
                                                               getLimit(limit));
    translateRoleMentions(kudos.toArray(new Kudos[0]));
    kudosList.setKudos(kudos);
    return kudosList;
  }

  @PostMapping
  @Secured("users")
  @Operation(
             summary = "Creates new Kudos",
             method = "POST",
             description = "Creates new Kudos and returns an empty response")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Kudos createKudos(
                           @RequestBody(description = "Kudos object to create", required = true)
                           @org.springframework.web.bind.annotation.RequestBody
                           Kudos kudos) {
    if (StringUtils.isBlank(kudos.getReceiverId()) || StringUtils.isBlank(kudos.getReceiverType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ReceiverId  and ReceiverType are mandatory");
    }
    if (StringUtils.isBlank(kudos.getEntityId()) || StringUtils.isBlank(kudos.getEntityType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Bad request sent to server with empty 'attached entity id or type'");
    }
    try {
      kudos.setSenderId(getCurrentUserId());
      Kudos kudosSent = kudosService.createKudos(kudos, getCurrentUserId());
      translateRoleMentions(kudosSent);
      return kudosSent;
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping("{kudosId}")
  @Secured("users")
  @Operation(summary = "Cancels a sent kudos", method = "DELETE", description = "Cancels a sent kudos")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "404", description = "Object not found"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public void deleteKudos(
                          @Parameter(description = "Kudos technical identifier", required = true)
                          @PathVariable("kudosId")
                          long kudosId) {
    String currentUser = getCurrentUserId();
    try {
      kudosService.deleteKudosById(kudosId, currentUser);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (KudosAlreadyLinkedException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "KudosAlreadyLinked");
    }
  }

  @GetMapping("period")
  @Secured("users")
  @Operation(
             summary = "Get Kudos period of time by computing it using period type and a selected date",
             method = "GET",
             description = "Get Kudos period of time by computing it using period type and a selected date")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public KudosPeriod getPeriodDates(
                                    @Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period")
                                    @RequestParam("periodType")
                                    String periodType,
                                    @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds")
                                    @RequestParam("dateInSeconds")
                                    long dateInSeconds) {
    KudosPeriodType kudosPeriodType = getKudosPeriodType(periodType);
    return kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
  }

  private int getLimit(int limit) {
    if (limit <= 0) {
      limit = 10;
    }
    return limit;
  }

  private Long getActivityId(String commentId) {
    return (commentId == null || commentId.trim().isEmpty()) ? null : Long.valueOf(commentId.replace("comment", ""));
  }

  private void translateRoleMentions(Kudos... kudosList) {
    if (ArrayUtils.isEmpty(kudosList)) {
      return;
    }
    Locale userLocale = LocalizationFilter.getCurrentLocale();
    for (Kudos kudos : kudosList) {
      if (kudos != null) {
        kudos.setMessage(MentionUtils.substituteUsernames(kudos.getMessage(), userLocale));
      }
    }
  }

  private org.exoplatform.services.security.Identity getCurrentIdentity() {
    return ConversationState.getCurrent().getIdentity();
  }

  private String getCurrentIdentityId() {
    Identity identity = identityManager.getOrCreateUserIdentity(getCurrentUserId());
    return identity.getId();
  }

  private KudosPeriodType getKudosPeriodType(String periodType) {
    if (StringUtils.isBlank(periodType)) {
      return kudosService.getDefaultKudosPeriodType();
    } else {
      try {
        return KudosPeriodType.valueOf(periodType.toUpperCase());
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodType  '" + periodType + "' is not valid");
      }
    }
  }

}
