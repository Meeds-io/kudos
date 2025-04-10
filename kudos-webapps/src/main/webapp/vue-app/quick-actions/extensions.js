/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

extensionRegistry.registerExtension('QuickAction', 'Extension', {
  id: 'kudos',
  icon: 'fa-award',
  name: 'quickActions.kudos.name',
  description: 'quickActions.kudos.description',
  click: () => new Promise(resolve => {
    window.require(['SHARED/kudos'], () => {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
      }}));
      resolve();
    });
  }),
});

extensionRegistry.registerExtension('QuickAction', 'Extension', {
  id: 'kudosList',
  icon: 'fa-award',
  name: 'quickActions.kudosList.name',
  description: 'quickActions.kudosList.description',
  click: () => new Promise(resolve => {
    window.require(['SHARED/eXoVueI18n', 'PORTLET/gamification-portlets/myContributions'], exoi18n => initKudosListDrawer(exoi18n, resolve));
  }),
});

async function initKudosListDrawer(exoi18n, callback) {
  const appId = 'achievments-quick-action';
  if (!document.querySelector(`#${appId}`)) {
    const parent = document.createElement('div');
    parent.id = appId;
    document.querySelector('#vuetify-apps').appendChild(parent);
    await initKudosListDrawerApp(appId, exoi18n);
    await Vue.prototype.$utils.importSkin('portal', 'kudos');
  }
  document.dispatchEvent(new CustomEvent('quick-action-kudos-list-drawer'));
  callback();
}

function initKudosListDrawerApp(appId, exoi18n) {
  const lang = eXo.env.portal.language;
  const url = `/kudos/i18n/locale.addon.Kudos?lang=${lang}`;
  return new Promise(resolve => exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => Vue.createApp({
      template: `
        <kudos-overview-drawer
          id="${appId}"
          ref="drawer" />
      `,
      created() {
        document.addEventListener('quick-action-kudos-list-drawer', this.openDrawer);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        resolve();
      },
      beforeDestroy() {
        document.removeEventListener('quick-action-kudos-list-drawer', this.openDrawer);
      },
      methods: {
        openDrawer() {
          this.$refs.drawer.open('sent', eXo.env.portal.userIdentityId);
        },
      },
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Kudos List Quick Action')));
}
