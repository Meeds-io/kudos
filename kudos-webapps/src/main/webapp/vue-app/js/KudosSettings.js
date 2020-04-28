/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
export function initSettings() {
  return fetch(`/portal/rest/kudos/api/account/settings`, {credentials: 'include'})
    .then((resp) => resp && resp.ok && resp.json())
    .then((settings) => (window.kudosSettings = settings ? settings : {}))
    .then(() => getSettings())
    .then((settings) => {
      if (settings) {
        window.kudosSettings = {...window.kudosSettings, ...settings};
      }
    });
}

export function getSettings() {
  return fetch(`/portal/rest/kudos/api/settings`, {credentials: 'include'}).then((resp) => resp && resp.ok && resp.json());
}

export function saveSettings(settings) {
  if (settings) {
    return fetch(`/portal/rest/kudos/api/settings/save`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(settings),
    }).then((resp) => resp && resp.ok);
  } else {
    return Promise.resolve(null);
  }
}
