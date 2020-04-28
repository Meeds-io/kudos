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
import KudosOverview from './components/KudosOverview.vue';
import KudosOverviewCard from './components/KudosOverviewCard.vue';
import KudosOverviewDrawer from './components/KudosOverviewDrawer.vue';
import KudosOverviewItem from './components/KudosOverviewItem.vue';

const components = {
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
  'kudos-overview-drawer': KudosOverviewDrawer,
  'kudos-overview-item': KudosOverviewItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
