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
package io.meeds.kudos.notification.plugin;

import static io.meeds.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import io.meeds.kudos.BaseKudosTest;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

@SpringJUnitConfig(BaseKudosTest.class)
public class KudosActivityChildPluginTest extends BaseKudosTest {

  private static final long   KUDOS_BY_ACTIVITY_ID = 5l;

  private static final String KUDOS_MESSAGE        = "KUDOS_MESSAGE";

  @Mock
  private KudosService        kudosService;

  @Mock
  private NotificationContext ctx;

  @Mock
  private InitParams          initParams;

  @Mock
  private NotificationInfo    notification;

  @Mock
  private Kudos               kudos;

  @Test
  public void testGetId() {
    KudosActivityChildPlugin kudosActivityChildPlugin = new KudosActivityChildPlugin(initParams);
    kudosActivityChildPlugin.kudosService = kudosService;
    assertEquals(KUDOS_ACTIVITY_COMMENT_TYPE, kudosActivityChildPlugin.getId());
  }

  @Test
  public void testIsValid() {
    KudosActivityChildPlugin kudosActivityChildPlugin = new KudosActivityChildPlugin(initParams);
    kudosActivityChildPlugin.kudosService = kudosService;
    assertFalse(kudosActivityChildPlugin.isValid(null));
  }

  @Test
  public void testMakeContent() {
    KudosActivityChildPlugin kudosActivityChildPlugin = new KudosActivityChildPlugin(initParams);
    kudosActivityChildPlugin.kudosService = kudosService;
    when(kudosService.getKudosByActivityId(KUDOS_BY_ACTIVITY_ID)).thenReturn(kudos);
    when(kudos.getMessage()).thenReturn(KUDOS_MESSAGE);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    when(notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey())).thenReturn(String.valueOf(KUDOS_BY_ACTIVITY_ID));

    assertTrue(StringUtils.contains(kudosActivityChildPlugin.makeContent(ctx), KUDOS_MESSAGE));
  }

}
