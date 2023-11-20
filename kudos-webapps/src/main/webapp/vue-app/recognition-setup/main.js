/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import './initComponents.js';
import '../engagement-center/initComponents.js';

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('RecognitionSetup');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'recognitionSetup';
const lang = window?.eXo?.env?.portal?.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.RecognitionSetup-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        template: `<recognition-setup id="${appId}" />`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n
      }, `#${appId}`, 'Recognition Setup App');
    })
    .finally(() => Vue.prototype.$utils?.includeExtensions('recognitionSetupExtension'));
}