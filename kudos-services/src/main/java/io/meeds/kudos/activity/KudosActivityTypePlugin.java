/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.activity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.ActivityTypePlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.utils.MentionUtils;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;
import io.meeds.kudos.service.utils.Utils;

import jakarta.annotation.PostConstruct;

@Component
public class KudosActivityTypePlugin extends ActivityTypePlugin {

  @Autowired
  private ActivityManager activityManager;

  @Autowired
  private KudosService    kudosService;

  public KudosActivityTypePlugin() {
    super(initParams());
  }

  @PostConstruct
  public void init() {
    activityManager.addActivityTypePlugin(this);
  }

  @Override
  public boolean isEnableNotification(ExoSocialActivity activity, String username) {
    if (StringUtils.isBlank(username)) {
      return false;
    } else {
      Kudos kudos = this.kudosService.getKudosByActivityId(Long.parseLong(activity.getId().replace("comment", "")));
      return kudos != null
             && !StringUtils.equals(kudos.getReceiverId(), username)
             && !StringUtils.equals(kudos.getSenderId(), username);
    }
  }

  @Override
  public String getActivityTitle(ExoSocialActivity activity) {
    Kudos kudos = this.kudosService.getKudosByActivityId(Long.parseLong(activity.getId().replace("comment", "")));
    return kudos == null ? activity.getTitle() : MentionUtils.substituteUsernames(kudos.getMessage());
  }

  private static InitParams initParams() {
    InitParams initParams = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("type");
    param.setValue(Utils.KUDOS_ACTIVITY_COMMENT_TYPE);
    initParams.addParameter(param);
    param = new ValueParam();
    param.setName("enableNotification");
    param.setValue("true");
    initParams.addParameter(param);
    return initParams;
  }

}
