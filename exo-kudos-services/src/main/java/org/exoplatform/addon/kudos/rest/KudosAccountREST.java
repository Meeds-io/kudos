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

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.kudos.model.AccountDetail;
import org.exoplatform.addon.kudos.model.AccountSettings;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;

/**
 * This class provide a REST endpoint to retrieve detailed information about
 * users and spaces
 */
@Path("/kudos/api/account")
@RolesAllowed("users")
public class KudosAccountREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(KudosAccountREST.class);

  private KudosService     kudosService;

  public KudosAccountREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  /**
   * Retrieves the user or space details by username or spacePrettyName
   * 
   * @param id
   * @param type
   * @return
   */
  @Path("detailsById")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getAccountByTypeAndID(@QueryParam("type") String type, @QueryParam("id") String id) {
    if (StringUtils.isBlank(id) || StringUtils.isBlank(type)) {
      LOG.warn("Bad request sent to server with id '{}' and type '{}'", id, type);
      return Response.status(400).build();
    }
    if (OrganizationIdentityProvider.NAME.equals(type)) {
      type = USER_ACCOUNT_TYPE;
    } else if (SpaceIdentityProvider.NAME.equals(type)) {
      type = SPACE_ACCOUNT_TYPE;
    }
    AccountDetail accountDetail = kudosService.getAccountDetails(type, id);
    if (accountDetail == null) {
      LOG.warn("Identity not found with type '{}' and id '{}'", type, id);
      return Response.status(400).build();
    }
    return Response.ok(accountDetail).build();
  }

  /**
   * Retrieves the user settings for kudos
   * 
   * @return
   */
  @Path("settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getSettings() {
    AccountSettings accountDetail = kudosService.getAccountSettings(getCurrentUserId());
    if (accountDetail == null) {
      return Response.ok("{}").build();
    }
    return Response.ok(accountDetail).build();
  }

}
