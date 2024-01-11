/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.kudos.rest;

import static org.exoplatform.kudos.service.utils.Utils.getCurrentUserId;
import static org.exoplatform.kudos.service.utils.Utils.timeFromSeconds;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.kudos.exception.KudosAlreadyLinkedException;
import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.utils.MentionUtils;


@Path("/kudos/api/kudos")
@Tag(name = "/kudos/api/kudos", description = "Manages Kudos") // NOSONAR
@RolesAllowed("users")
public class KudosREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(KudosREST.class);

  private KudosService     kudosService;

  private IdentityManager  identityManager;

  public KudosREST(IdentityManager identityManager, KudosService kudosService) {
    this.identityManager = identityManager;
    this.kudosService = kudosService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Operation(
          summary = "Get Kudos list created in a period contained a selected date in seconds",
          method = "GET",
          description = "Get Kudos list created in a period contained a selected date in seconds and returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getKudosByPeriodOfDate(@Parameter(description = "Timestamp in seconds of date in the middle of selected period. If not defined, current time will be used.") @QueryParam("dateInSeconds") long dateInSeconds,
                                         @Parameter(description = "Limit of results to return") @QueryParam("limit") int limit) {
    if (dateInSeconds == 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriodOfDate(dateInSeconds, getLimit(limit));
      translateRoleMentions(allKudosByPeriod.toArray(new Kudos[0]));
      return Response.ok(allKudosByPeriod).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos list of period with date {}", dateInSeconds, e);
      return Response.serverError().build();
    }
  }

  @Path("byEntity")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Get Kudos list by entity type and id", method = "GET", description = "Get Kudos list by entity type and id and returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getEntityKudos(@Parameter(description = "kudos entity type (for example activity, comment...)", required = true) @QueryParam("entityType") String entityType,
                                 @Parameter(description = "kudos entity id", required = true) @QueryParam("entityId") String entityId,
                                 @Parameter(description = "Limit of results to return") @QueryParam("limit") int limit) {
    if (StringUtils.isBlank(entityType) || StringUtils.isBlank(entityId)) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByEntity = kudosService.getKudosByEntity(entityType, entityId, getLimit(limit));
      translateRoleMentions(allKudosByEntity.toArray(new Kudos[0]));
      return Response.ok(allKudosByEntity).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos entity of entity {}/{}", entityType, entityId, e);
      return Response.serverError().build();
    }
  }

  @Path("byActivity/{activityId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Get Kudos by its generated comment or activity id", method = "GET", description = "Get Kudos by its generated comment or activity id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "404", description = "Entity Not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getKudosByActivityId(
                                       @Parameter(description = "kudos activity or comment identifier", required = true)
                                       @PathParam("activityId")
                                       String activityId) {
    if (StringUtils.isBlank(activityId)) {
      LOG.warn("Bad request sent to server with empty 'attached activityId'");
      return Response.status(Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    try {
      Kudos kudos = kudosService.getKudosByActivityId(getActivityId(activityId), currentUser);
      translateRoleMentions(kudos);
      return Response.ok(kudos).build();
    } catch (IllegalAccessException e) {
      LOG.error("Access denied to user {} to access Kudos of activity by id {}", currentUser.getUserId(), activityId);
      return Response.status(Status.NOT_FOUND).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos by activity Id {}", activityId, e);
      return Response.serverError().build();
    }
  }

  @Path("byActivity/{activityId}/all")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
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
  public Response getKudosListOfActivity(@Parameter(description = "kudos parent activity identifier", required = true)
                                         @PathParam("activityId")
                                         String activityId) {
    if (StringUtils.isBlank(activityId)) {
      LOG.warn("Bad request sent to server with empty 'activityId'");
      return Response.status(Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    try {
      List<Kudos> kudosList = kudosService.getKudosListOfActivity(activityId, currentUser);
      translateRoleMentions(kudosList.toArray(new Kudos[0]));
      return Response.ok(kudosList).build();
    } catch (IllegalAccessException e) {
      LOG.error("Access denied to user {} to access Kudos of parent activity by id {}", currentUser.getUserId(), activityId);
      return Response.status(Status.NOT_FOUND).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos by parent activity Id {}", activityId, e);
      return Response.serverError().build();
    }
  }

  @Path("byEntity/sent/count")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(
      summary = "Get Kudos count by entity and current user as sender",
      method = "GET",
      description = "Get Kudos count by entity and current user as sender"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error") }
  )
  public Response countKudosByEntityAndSender(
                                              @Parameter(
                                                  description = "kudos entity type (for example activity, comment...)",
                                                  required = true
                                              )
                                              @QueryParam("entityType")
                                              String entityType,
                                              @Parameter(description = "kudos entity id", required = true)
                                              @QueryParam("entityId")
                                              String entityId) {
    if (StringUtils.isBlank(entityType) || StringUtils.isBlank(entityId)) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    String currentUsername = getCurrentUserId();
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUsername);
    if (identity == null) {
      return Response.status(400).entity("Can't find current user identity").build();
    }
    try {
      long count = kudosService.countKudosByEntityAndSender(entityType, entityId, identity.getId());
      return Response.ok(String.valueOf(count)).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos entity of entity {}/{}", entityType, entityId, e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("byDates")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Operation(
          summary = "Get Kudos list created between start and end dates in seconds",
          method = "GET",
          description = "Get Kudos list created between start and end dates in seconds")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getKudosByDates(@QueryParam("startDateInSeconds") long startDateInSeconds,
                                  @QueryParam("endDateInSeconds") long endDateInSeconds,
                                  @Parameter(description = "Limit of results to return") @QueryParam("limit") int limit) {
    if (startDateInSeconds == 0 || endDateInSeconds == 0) {
      LOG.warn("Bad request sent to server with empty 'start or end' dates parameter");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriod(startDateInSeconds, endDateInSeconds, getLimit(limit));
      translateRoleMentions(allKudosByPeriod.toArray(new Kudos[0]));
      return Response.ok(allKudosByPeriod).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos list of period: from {} to {}", startDateInSeconds, endDateInSeconds, e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{identityId}/received")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Retrieve the list of received Kudos by a user or space in a selected period",
          method = "GET",
          description = "Retrieve the list of received Kudos by a user or space in a selected period")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getReceivedKudosByPeriod(@Parameter(description = "User or space identity technical id", required = true) @PathParam("identityId") long identityId,
                                           @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds", required = true) @QueryParam("dateInSeconds") long dateInSeconds,
                                           @Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period", required = true) @QueryParam("periodType") String periodType,
                                           @Parameter(description = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved", required = true) @QueryParam("limit") int limit,
                                           @Parameter(description = "Whether return size of received kudos, default = false") @QueryParam("returnSize") boolean returnSize) {
    if (identityId <= 0) {
      return Response.status(400).entity("identityId is mandatory").build();
    }
    if (dateInSeconds < 0) {
      return Response.status(400).entity("dateInSeconds parameter should be a positive number").build();
    }
    if (limit < 0) {
      return Response.status(400).entity("limit parameter should be a positive number").build();
    }
    if (!returnSize && limit == 0) {
      return Response.status(400)
                     .entity("you should whether use 'limit' to get a list of kudos or 'returnSize' to return the size")
                     .build();
    }

    Identity identity = identityManager.getIdentity(String.valueOf(identityId));
    if (identity == null) {
      return Response.status(400).entity("Can't find identity with id " + identityId).build();
    }

    if (dateInSeconds == 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }

    KudosPeriodType kudosPeriodType = null;
    if (StringUtils.isBlank(periodType)) {
      kudosPeriodType = kudosService.getDefaultKudosPeriodType();
    } else {
      try {
        kudosPeriodType = KudosPeriodType.valueOf(periodType.toUpperCase());
      } catch (Exception e) {
        return Response.status(400).entity("periodType  '" + periodType + "' is not valid").build();
      }
    }

    KudosPeriod period = kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
    KudosList kudosList = new KudosList();
    if (returnSize) {
      long size = kudosService.countKudosByPeriodAndReceiver(identityId,
                                                             period.getStartDateInSeconds(),
                                                             period.getEndDateInSeconds());
      kudosList.setSize(size);
      if (size == 0 || limit == 0) {
        return Response.ok(kudosList).build();
      }
    }
    List<Kudos> kudos = kudosService.getKudosByPeriodAndReceiver(identityId,
                                                                 period.getStartDateInSeconds(),
                                                                 period.getEndDateInSeconds(),
                                                                 getLimit(limit));
    translateRoleMentions(kudos.toArray(new Kudos[0]));
    kudosList.setKudos(kudos);
    return Response.ok(kudosList).build();
  }

  @Path("{identityId}/sent")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
          summary = "Retrieve the list of sent Kudos for a user in a selected period",
          method = "GET",
          description = "Retrieve the list of sent Kudos for a user in a selected period")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getSentKudosByPeriod(@Parameter(description = "User or space identity technical id", required = true) @PathParam("identityId") long identityId,
                                       @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds") @QueryParam("dateInSeconds") long dateInSeconds,
                                       @Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period") @QueryParam("periodType") String periodType,
                                       @Parameter(description = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved") @QueryParam("limit") int limit,
                                       @Parameter(description = "Whether return size of sent kudos, default = false") @QueryParam("returnSize") boolean returnSize) {
    if (identityId <= 0) {
      return Response.status(400).entity("identityId is mandatory").build();
    }
    if (dateInSeconds < 0) {
      return Response.status(400).entity("dateInSeconds parameter should be a positive number").build();
    }
    if (limit < 0) {
      return Response.status(400).entity("limit parameter should be a positive number").build();
    }
    if (!returnSize && limit == 0) {
      return Response.status(400)
                     .entity("you should whether use 'limit' to get a list of kudos or 'returnSize' to return the size")
                     .build();
    }

    Identity identity = identityManager.getIdentity(String.valueOf(identityId));
    if (identity == null) {
      return Response.status(400).entity("Can't find identity with id " + identityId).build();
    }

    if (dateInSeconds == 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }

    KudosPeriodType kudosPeriodType = null;
    if (StringUtils.isBlank(periodType)) {
      kudosPeriodType = kudosService.getDefaultKudosPeriodType();
    } else {
      try {
        kudosPeriodType = KudosPeriodType.valueOf(periodType.toUpperCase());
      } catch (Exception e) {
        return Response.status(400).entity("periodType  '" + periodType + "' is not valid").build();
      }
    }

    KudosPeriod period = kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
    KudosList kudosList = new KudosList();
    if (returnSize) {
      long size = kudosService.countKudosByPeriodAndSender(identityId,
                                                           period.getStartDateInSeconds(),
                                                           period.getEndDateInSeconds());
      kudosList.setSize(size);
      if (size == 0 || limit == 0) {
        return Response.ok(kudosList).build();
      }
    }

    List<Kudos> kudos = kudosService.getKudosByPeriodAndSender(identityId,
                                                               period.getStartDateInSeconds(),
                                                               period.getEndDateInSeconds(),
                                                               getLimit(limit));
    translateRoleMentions(kudos.toArray(new Kudos[0]));
    kudosList.setKudos(kudos);
    return Response.ok(kudosList).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Creates new Kudos",
      method = "POST",
      description = "Creates new Kudos and returns an empty response"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response createKudos(@RequestBody(description = "Kudos object to create", required = true) Kudos kudos) {
    if (kudos == null) {
      LOG.warn("Bad request sent to server with empty kudos");
      return Response.status(400).build();
    }
    if (StringUtils.isBlank(kudos.getReceiverId()) || StringUtils.isBlank(kudos.getReceiverType())) {
      LOG.warn("Bad request sent to server with empty 'receiver id or type'");
      return Response.status(400).build();
    }
    if (StringUtils.isNotBlank(kudos.getSenderId())) {
      LOG.warn("Bad request sent to server with a preset 'sender'");
      return Response.status(400).build();
    }
    if (StringUtils.isBlank(kudos.getEntityId()) || StringUtils.isBlank(kudos.getEntityType())) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    try {
      kudos.setSenderId(getCurrentUserId());
      Kudos kudosSent = kudosService.createKudos(kudos, getCurrentUserId());
      translateRoleMentions(kudosSent);
      return Response.ok(kudosSent).build();
    } catch (Exception e) {
      LOG.warn("Error saving kudos: {}", kudos, e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("{kudosId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Cancels a sent kudos", method = "DELETE", description = "Cancels a sent kudos")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "404", description = "Object not found"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response deleteKudos(@Parameter(description = "Kudos technical identifier", required = true)
                              @PathParam("kudosId") long kudosId) {

    String currentUser = getCurrentUserId();

    try {
      kudosService.deleteKudosById(kudosId, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      LOG.debug("User '{}' doesn't have enough privileges to delete kudos with id {}", currentUser, kudosId, e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      LOG.debug("User '{}' attempts to delete a not existing kudos '{}'", currentUser, e);
      return Response.status(Response.Status.NOT_FOUND).entity("kudos not found").build();
    } catch (KudosAlreadyLinkedException e) {
      LOG.debug("User '{}' attempts to delete a kudos '{}' already linked to kudos entities", currentUser, e);
      return Response.status(Response.Status.UNAUTHORIZED).entity("KudosAlreadyLinked").build();
    } catch (Exception e) {
      LOG.warn("Error canceling kudos: {}", e);
      return Response.serverError().build();
    }
  }

  @Path("period")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
          summary = "Get Kudos period of time by computing it using period type and a selected date",
          method = "GET",
          description = "Get Kudos period of time by computing it using period type and a selected date")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getPeriodDates(@Parameter(description = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period") @QueryParam("periodType") String periodType,
                                 @Parameter(description = "Date in the middle of a period defined using a timestamp in seconds") @QueryParam("dateInSeconds") long dateInSeconds) {
    if (dateInSeconds == 0) {
      LOG.warn("Bad request sent to server with empty 'dateInSeconds' parameter");
      return Response.status(400).build();
    }
    if (StringUtils.isBlank(periodType)) {
      LOG.warn("Bad request sent to server with empty 'periodType' parameter");
      return Response.status(400).build();
    }
    try {
      KudosPeriodType kudosPeriodType = KudosPeriodType.valueOf(periodType);
      KudosPeriod kudosPeriod = kudosPeriodType.getPeriodOfTime(timeFromSeconds(dateInSeconds));
      return Response.ok(kudosPeriod.toString()).build();
    } catch (Exception e) {
      LOG.warn("Error getting period dates of type {} and date {}", periodType, dateInSeconds, e);
      return Response.serverError().build();
    }
  }

  private int getLimit(int limit) {
    if (limit <= 0) {
      limit = 1000;
    }
    return limit;
  }

  private Long getActivityId(String commentId) {
    return (commentId == null || commentId.trim().isEmpty()) ? null : Long.valueOf(commentId.replace("comment", ""));
  }

  private void translateRoleMentions(Kudos ...kudosList) {
    if (ArrayUtils.isEmpty(kudosList)) {
      return;
    }
    Locale userLocale = LocalizationFilter.getCurrentLocale();
    for (Kudos kudos : kudosList) {
      if (kudos != null) {
        kudos.setMessage(MentionUtils.substituteRoleWithLocale(kudos.getMessage(),
                                                               userLocale));
      }
    }
  }

}
