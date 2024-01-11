/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.kudos.notification.plugin;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;

import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class KudosActivityChildPlugin extends AbstractNotificationChildPlugin {

  private KudosService kudosService;

  public KudosActivityChildPlugin(KudosService kudosService, InitParams initParams) {
    super(initParams);
    this.kudosService = kudosService;
  }

  @Override
  public String makeContent(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();
    if (notification == null) {
      return "";
    }

    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    Kudos kudos = kudosService.getKudosByActivityId(Long.parseLong(activityId.replace("comment", "")));
    if (kudos == null) {
      return "";
    }
    String language = getLanguage(notification);
    String message = MentionUtils.substituteRoleWithLocale(kudos.getMessage(),
                                                           Locale.forLanguageTag(language));
    TemplateContext templateContext = new TemplateContext(getId(), language);
    templateContext.put("MESSAGE", message);
    return TemplateUtils.processGroovy(templateContext);
  }

  @Override
  public String getId() {
    return KUDOS_ACTIVITY_COMMENT_TYPE;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return false;
  }

}
