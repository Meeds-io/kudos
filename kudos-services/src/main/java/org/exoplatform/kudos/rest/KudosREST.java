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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import io.swagger.annotations.*;

@Path("/kudos/api/kudos")
@Api(value = "/kudos/api/kudos", description = "Manages Kudos") // NOSONAR
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
  @ApiOperation(value = "Get Kudos list created in a period contained a selected date in seconds", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getKudosByPeriodOfDate(@ApiParam(value = "Timestamp in seconds of date in the middle of selected period. If not defined, current time will be used.", required = false) @QueryParam("dateInSeconds") long dateInSeconds,
                                         @ApiParam(value = "Limit of results to return", required = false) @QueryParam("limit") int limit) {
    if (dateInSeconds == 0) {
      dateInSeconds = System.currentTimeMillis() / 1000;
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriodOfDate(dateInSeconds, getLimit(limit));
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
  @ApiOperation(value = "Get Kudos list by entity type and id", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getEntityKudos(@ApiParam(value = "kudos entity type (for example activity, comment...)", required = true) @QueryParam("entityType") String entityType,
                                 @ApiParam(value = "kudos entity id", required = true) @QueryParam("entityId") String entityId,
                                 @ApiParam(value = "Limit of results to return", required = false) @QueryParam("limit") int limit) {
    if (StringUtils.isBlank(entityType) || StringUtils.isBlank(entityId)) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByEntity = kudosService.getKudosByEntity(entityType, entityId, getLimit(limit));
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
  @ApiOperation(value = "Get Kudos by its generated comment or activity id", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns Kudos object")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Entity Not found"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getKudosByActivityId(
                                       @ApiParam(value = "kudos activity or comment identifier", required = true)
                                       @PathParam("activityId")
                                       String activityId) {
    if (StringUtils.isBlank(activityId)) {
      LOG.warn("Bad request sent to server with empty 'attached activityId'");
      return Response.status(Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    try {
      Kudos kudos = kudosService.getKudosByActivityId(getActivityId(activityId), currentUser);
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
  @ApiOperation(value = "Get Kudos List attached to a parent activity, whether the activity itself or in a comment", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns Kudos List")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Entity Not found"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getKudosListOfActivity(@ApiParam(value = "kudos parent activity identifier", required = true)
                                         @PathParam("activityId")
                                         String activityId) {
    if (StringUtils.isBlank(activityId)) {
      LOG.warn("Bad request sent to server with empty 'activityId'");
      return Response.status(Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    try {
      List<Kudos> kudosList = kudosService.getKudosListOfActivity(activityId, currentUser);
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
  @ApiOperation(
      value = "Get Kudos count by entity and current user as sender",
      httpMethod = "GET",
      response = Response.class,
      produces = "text/plain",
      notes = "returns Kudos count"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 500, message = "Internal server error") }
  )
  public Response countKudosByEntityAndSender(
                                              @ApiParam(
                                                  value = "kudos entity type (for example activity, comment...)",
                                                  required = true
                                              )
                                              @QueryParam("entityType")
                                              String entityType,
                                              @ApiParam(value = "kudos entity id", required = true)
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
  @ApiOperation(value = "Get Kudos list created between start and end dates in seconds", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getKudosByDates(@QueryParam("startDateInSeconds") long startDateInSeconds,
                                  @QueryParam("endDateInSeconds") long endDateInSeconds,
                                  @ApiParam(value = "Limit of results to return", required = false) @QueryParam("limit") int limit) {
    if (startDateInSeconds == 0 || endDateInSeconds == 0) {
      LOG.warn("Bad request sent to server with empty 'start or end' dates parameter");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getKudosByPeriod(startDateInSeconds, endDateInSeconds, getLimit(limit));
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
  @ApiOperation(value = "Retrieve the list of received Kudos by a user or space in a selected period", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 500, message = "Internal server error")
  })
  public Response getReceivedKudosByPeriod(@ApiParam(value = "User or space identity technical id", required = true) @PathParam("identityId") long identityId,
                                           @ApiParam(value = "Date in the middle of a period defined using a timestamp in seconds", required = true) @QueryParam("dateInSeconds") long dateInSeconds,
                                           @ApiParam(value = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period", required = true) @QueryParam("periodType") String periodType,
                                           @ApiParam(value = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved", required = true) @QueryParam("limit") int limit,
                                           @ApiParam(value = "Whether return size of received kudos, default = false", required = false) @QueryParam("returnSize") boolean returnSize) {
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
    kudosList.setKudos(kudos);
    return Response.ok(kudosList).build();
  }

  @Path("{identityId}/sent")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieve the list of sent Kudos for a user in a selected period", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getSentKudosByPeriod(@ApiParam(value = "User or space identity technical id", required = true) @PathParam("identityId") long identityId,
                                       @ApiParam(value = "Date in the middle of a period defined using a timestamp in seconds", required = false) @QueryParam("dateInSeconds") long dateInSeconds,
                                       @ApiParam(value = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period", required = false) @QueryParam("periodType") String periodType,
                                       @ApiParam(value = "Limit of kudos to retrieve, if equal to 0, no kudos will be retrieved", required = false) @QueryParam("limit") int limit,
                                       @ApiParam(value = "Whether return size of sent kudos, default = false", required = false) @QueryParam("returnSize") boolean returnSize) {
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
    kudosList.setKudos(kudos);
    return Response.ok(kudosList).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Creates new Kudos",
      httpMethod = "POST",
      response = Response.class,
      consumes = "application/json",
      produces = "application/json",
      notes = "returns empty response"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response createKudos(@ApiParam(value = "Kudos object to create", required = true) Kudos kudos) {
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
      return Response.ok(kudosSent).build();
    } catch (Exception e) {
      LOG.warn("Error saving kudos: {}", kudos, e);
      return Response.serverError().build();
    }
  }

  @Path("period")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Kudos period of time by computing it using period type and a selected date", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns Kudos period object")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getPeriodDates(@ApiParam(value = "Period type, can be: WEEK, MONTH, QUARTER, SEMESTER and YEAR. Default is the same as configured period", required = false) @QueryParam("periodType") String periodType,
                                 @ApiParam(value = "Date in the middle of a period defined using a timestamp in seconds", required = false) @QueryParam("dateInSeconds") long dateInSeconds) {
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
}
