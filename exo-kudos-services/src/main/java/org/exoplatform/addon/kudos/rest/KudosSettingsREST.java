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
package org.exoplatform.addon.kudos.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.kudos.model.GlobalSettings;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to manage global settings
 */
@Path("/kudos/api/settings")
@RolesAllowed("users")
public class KudosSettingsREST implements ResourceContainer {

  private KudosService kudosService;

  public KudosSettingsREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  /**
   * @return global settings of Kudos application
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getSettings() {
    return Response.ok(kudosService.getGlobalSettings().toString()).build();
  }

  /**
   * Save global settings of Kudos application
   * 
   * @param settings
   * @return
   */
  @Path("save")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  public Response saveSettings(GlobalSettings settings) {
    kudosService.saveGlobalSettings(settings);
    return Response.ok().build();
  }

}
