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

import static org.exoplatform.addon.kudos.service.utils.Utils.getCurrentUserId;

import java.time.YearMonth;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to retrieve detailed information about
 * kudos
 */
@Path("/kudos/api/kudos")
@RolesAllowed("users")
public class KudosREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(KudosREST.class);

  private KudosService     kudosService;

  public KudosREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  /**
   * Save kudos
   * 
   * @param kudos
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("saveKudos")
  @RolesAllowed("users")
  public Response saveKudos(Kudos kudos) {
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

    kudos.setSenderId(getCurrentUserId());
    try {
      kudosService.sendKudos(getCurrentUserId(), kudos);
    } catch (Exception e) {
      LOG.warn("Error saving kudos", e);
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  /**
   * Retrieves the user/space kudos
   * 
   * @return
   */
  @Path("getKudos")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getKudos(@QueryParam("identityId") String identityId) {
    if (StringUtils.isBlank(identityId)) {
      LOG.warn("Bad request sent to server with empty 'identity id'");
      return Response.status(400).build();
    }
    List<Kudos> allKudosBySender = kudosService.getAllKudosByMonthAndSender(YearMonth.now(), identityId);
    return Response.ok(allKudosBySender).build();
  }

  /**
   * Retrieves the user settings for kudos
   * 
   * @return
   */
  @Path("getEntityKudos")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getEntityKudos(@QueryParam("entityType") String entityType, @QueryParam("entityId") String entityId) {
    if (StringUtils.isBlank(entityType) || StringUtils.isBlank(entityId)) {
      LOG.warn("Bad request sent to server with empty 'attached entity id or type'");
      return Response.status(400).build();
    }
    List<Kudos> allKudosByEntity = kudosService.getAllKudosByEntity(entityType, entityId);
    return Response.ok(allKudosByEntity).build();
  }

}
