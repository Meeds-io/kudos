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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.kudos.model.GlobalSettings;
import io.meeds.kudos.service.KudosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("settings")
@Tag(name = "/kudos/rest/settings", description = "Manages Kudos global settings")
public class KudosSettingsREST {

  @Autowired
  private KudosService kudosService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Get Kudos global settings", method = "GET", description = "Get Kudos global settings")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public GlobalSettings getSettings() {
    return kudosService.getGlobalSettings();
  }

  @PostMapping
  @Secured("administrators")
  @Operation(
             summary = "Saves Kudos global settings",
             method = "POST",
             description = "Saves Kudos global settings and returns an empty response")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public void saveSettings(
                           @RequestBody
                           GlobalSettings settings) {
    kudosService.saveGlobalSettings(settings);
  }

}
