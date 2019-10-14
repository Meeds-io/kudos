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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.kudos.model.AccountSettings;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/kudos/api/account")
@Api(value = "/kudos/api/account", description = "Retrieve Kudos settings for users and spaces") // NOSONAR
@RolesAllowed("users")
public class KudosAccountREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(KudosAccountREST.class);

  private KudosService     kudosService;

  public KudosAccountREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  @Path("settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves user/space settings for kudos", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns account settings object")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getSettings() {
    try {
      AccountSettings accountDetail = kudosService.getAccountSettings(getCurrentUserId());
      if (accountDetail == null) {
        accountDetail = new AccountSettings();
      }
      return Response.ok(accountDetail).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos settings", e);
      return Response.serverError().build();
    }
  }

  @Path("isAuthorized")
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Checks if username is authorized to use Kudos", httpMethod = "GET", response = Response.class, notes = "returns empty response")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response isAuthorized(@ApiParam(value = "User login", required = true) @QueryParam("username") String username) {
    if (StringUtils.isBlank(username)) {
      LOG.warn("Bad request sent to server with empty 'username'");
      return Response.status(400).build();
    }
    try {
      if (kudosService.isAuthorized(username)) {
        return Response.ok().build();
      } else {
        return Response.status(403).build();
      }
    } catch (Exception e) {
      LOG.warn("Error getting kudos authorization for user {}", username, e);
      return Response.serverError().build();
    }
  }

}
