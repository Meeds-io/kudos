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
package io.meeds.test.kudos.mock;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("all")
public class SpaceServiceMock implements SpaceService {

  @Getter
  @Setter
  private static String redactor;

  @Getter
  @Setter
  private static String member;

  public Space getSpaceByPrettyName(String spacePrettyName) {
    Space space = new Space();
    space.setPrettyName(spacePrettyName);
    space.setId(String.valueOf(spacePrettyName.charAt(spacePrettyName.length() - 1)));
    space.setGroupId("/spaces/" + spacePrettyName);
    return space;
  }

  public Space getSpaceById(String spaceId) {
    return null;
  }

  public boolean isManager(Space space, String userId) {
    return false;
  }

  @Override
  public boolean canRedactOnSpace(Space space, String username) {
    return space != null && redactor != null && StringUtils.equals(username, redactor);
  }

  @Override
  public boolean canViewSpace(Space space, String username) {
    return space != null && member != null && StringUtils.equals(username, member);
  }

  public boolean isSuperManager(String userId) {
    return false;
  }

}
