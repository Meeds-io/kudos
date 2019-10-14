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

import org.apache.commons.lang.StringUtils;

import org.exoplatform.kudos.model.*;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/kudos/api/kudos")
@Api(value = "/kudos/api/kudos", description = "Manages Kudos") // NOSONAR
@RolesAllowed("users")
public class KudosREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(KudosREST.class);

  private KudosService     kudosService;

  public KudosREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("createKudos")
  @RolesAllowed("users")
  @ApiOperation(value = "Creates new Kudos", httpMethod = "POST", response = Response.class, consumes = "application/json", notes = "returns empty response")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Request fulfilled"),
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
      kudosService.sendKudos(kudos, getCurrentUserId());
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.warn("Error saving kudos: {}", kudos, e);
      return Response.serverError().build();
    }
  }

  @Path("getKudos")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Kudos list by sender login in current period", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getKudos(@ApiParam(value = "kudos sender user login", required = true) @QueryParam("senderId") String senderId) {
    if (StringUtils.isBlank(senderId)) {
      LOG.warn("Bad request sent to server with empty 'identity id'");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosBySender = kudosService.getAllKudosBySenderInCurrentPeriod(senderId);
      return Response.ok(allKudosBySender).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos list of identity {}", senderId, e);
      return Response.serverError().build();
    }
  }

  @Path("getEntityKudos")
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
                                 @ApiParam(value = "kudos entity id", required = true) @QueryParam("entityId") String entityId) {
    if (StringUtils.isBlank(entityType) || StringUtils.isBlank(entityId)) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByEntity = kudosService.getAllKudosByEntity(entityType, entityId);
      return Response.ok(allKudosByEntity).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos entity of entity {}/{}", entityType, entityId, e);
      return Response.serverError().build();
    }
  }

  @Path("getAllKudosByPeriod")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Get Kudos list created between start and end dates in seconds", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getAllKudosByPeriod(@QueryParam("startDateInSeconds") long startDateInSeconds,
                                      @QueryParam("endDateInSeconds") long endDateInSeconds) {
    if (startDateInSeconds == 0 || endDateInSeconds == 0) {
      LOG.warn("Bad request sent to server with empty 'start or end' dates parameter");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getAllKudosByPeriod(startDateInSeconds, endDateInSeconds);
      return Response.ok(allKudosByPeriod).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos list of period: from {} to {}", startDateInSeconds, endDateInSeconds, e);
      return Response.serverError().build();
    }
  }

  @Path("getAllKudosByPeriodOfDate")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Get Kudos list created in a period contained a selected date in seconds", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns list of Kudos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getAllKudosByPeriodOfDate(@QueryParam("dateInSeconds") long dateInSeconds) {
    if (dateInSeconds == 0) {
      LOG.warn("Bad request sent to server with empty 'dateInSeconds' parameter");
      return Response.status(400).build();
    }
    try {
      List<Kudos> allKudosByPeriod = kudosService.getAllKudosByPeriodOfDate(dateInSeconds);
      return Response.ok(allKudosByPeriod).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos list of period with date {}", dateInSeconds, e);
      return Response.serverError().build();
    }
  }

  @Path("getPeriodDates")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Kudos period of time by computing it using periodType and a selected date", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns KudosPeriod object")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getPeriodDates(@QueryParam("periodType") String periodType, @QueryParam("dateInSeconds") long dateInSeconds) {
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
}
