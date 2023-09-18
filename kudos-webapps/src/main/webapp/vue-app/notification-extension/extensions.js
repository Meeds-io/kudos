/*
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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

extensionRegistry.registerExtension('WebNotification', 'notification-group-extension', {
  rank: 40,
  name: 'kudos',
  plugins: [
    'KudosActivityReceiverNotificationPlugin'
  ],
  icon: 'fa-award',
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'KudosActivityReceiverNotificationPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-kudos-received'],
});
extensionRegistry.registerComponent('WebNotification', 'NewUserPlugin-actions', {
  type: 'NewUserKudosButton',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-kudos-button'],
});
extensionRegistry.registerExtension('WebNotification', 'activity-notification-exokudos:activity', {
  id: 'KudosActivity',
  rank: 10,
  isEnabled: () => true,
  getContent: (_notification, activity) => activity?.templateParams?.kudosMessage,
});
