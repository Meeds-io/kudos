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
package io.meeds.kudos.notification.provider;

import static io.meeds.kudos.service.utils.Utils.KUDOS_RECEIVER_NOTIFICATION_ID;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.common.xmlprocessor.XMLProcessor;

import io.meeds.kudos.notification.builder.KudosTemplateBuilder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = KUDOS_RECEIVER_NOTIFICATION_ID, template = "war:/conf/kudos/templates/notification/push/KudosReceiverPushPlugin.gtmpl") })
public class MobilePushTemplateProvider extends TemplateProvider {

  private XMLProcessor xmlProcessor;

  public MobilePushTemplateProvider(InitParams initParams , XMLProcessor xmlProcessor) {
    super(initParams);
    this.xmlProcessor = xmlProcessor;
    this.templateBuilders.put(PluginKey.key(KUDOS_RECEIVER_NOTIFICATION_ID), new KudosTemplateBuilder(this, true , this.xmlProcessor));
  }

}
