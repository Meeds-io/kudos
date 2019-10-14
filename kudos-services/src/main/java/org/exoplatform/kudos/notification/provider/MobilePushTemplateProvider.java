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
package org.exoplatform.kudos.notification.provider;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_RECEIVER_NOTIFICATION_ID;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.notification.builder.KudosTemplateBuilder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = KUDOS_RECEIVER_NOTIFICATION_ID, template = "war:/conf/kudos/templates/notification/push/KudosReceiverPushPlugin.gtmpl") })
public class MobilePushTemplateProvider extends TemplateProvider {

  public MobilePushTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(KUDOS_RECEIVER_NOTIFICATION_ID), new KudosTemplateBuilder(this, true));
  }

}
