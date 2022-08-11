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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.kudos.model.GlobalSettings;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


@Path("/kudos/api/settings")
@Tag(name = "/kudos/api/settings", description = "Manages Kudos global settings")
@RolesAllowed("users")
public class KudosSettingsREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(KudosSettingsREST.class);

  private KudosService     kudosService;

  public KudosSettingsREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Get Kudos global settings", method = "GET", description = "Get Kudos global settings")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getSettings() {
    return Response.ok(kudosService.getGlobalSettings().toString()).build();
  }

  @Path("save")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Operation(
          summary = "Saves Kudos global settings",
          method = "POST",
          description = "Saves Kudos global settings and returns an empty response")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response saveSettings(GlobalSettings settings) {
    if (settings == null) {
      LOG.warn("Bad request sent to server with empty 'settings' parameter");
      return Response.status(400).build();
    }
    try {
      kudosService.saveGlobalSettings(settings);
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.warn("Error saving kudos settings: {}", settings, e);
      return Response.serverError().build();
    }
  }

}
