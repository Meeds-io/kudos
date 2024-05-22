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
package io.meeds.kudos.activity.processor;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.utils.MentionUtils;

import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.service.KudosService;

import jakarta.annotation.PostConstruct;

@Component
public class ActivityKudosProcessor extends BaseActivityProcessorPlugin {

  @Autowired
  private ActivityManager         activityManager;

  @Autowired
  private UserPortalConfigService userPortalConfigService;

  @Autowired
  private KudosService            kudosService;

  private String                  defaultPortal;

  public ActivityKudosProcessor() {
    super(initParams());
  }

  @Override
  public String getName() {
    return "ActivityKudosProcessor";
  }

  @PostConstruct
  public void init() {
    defaultPortal = userPortalConfigService.getMetaPortal();
    activityManager.addProcessor(this);
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity.isComment()) {
      return;
    }
    if (activity.getLinkedProcessedEntities() == null) {
      activity.setLinkedProcessedEntities(new HashMap<>());
    }
    @SuppressWarnings("unchecked")
    List<Kudos> linkedKudosList = (List<Kudos>) activity.getLinkedProcessedEntities().get("kudosList");
    if (linkedKudosList == null) {
      linkedKudosList = kudosService.getKudosListOfActivity(activity.getId());
      activity.getLinkedProcessedEntities().put("kudosList", linkedKudosList);
    }

    if (linkedKudosList != null) {
      for (Kudos kudos : linkedKudosList) {
        kudos.setMessage(MentionUtils.substituteUsernames(defaultPortal, kudos.getMessage()));
      }
    }
  }

  private static InitParams initParams() {
    InitParams initParams = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("priority");
    param.setValue("20");
    initParams.addParameter(param);
    return initParams;
  }

}
