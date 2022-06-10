<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <a
    v-if="kudosNumber>0"
    class="my-1 me-2 KudosNumber"
    @click="openKudosList">
    {{ kudosNumber }} {{ $t('exoplatform.kudos.label.kudos') }}</a>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    kudosNumber: 0,
  }),
  created() {
    this.$root.$on('kudos-refresh-comment', this.getKudosCount);
    this.getKudosCount();
  },
  methods: {
    getKudosCount() {
      return this.$kudosService.computeActivityKudosList(this.activity)
        .then(() => this.kudosNumber = this.activity.linkedKudosList.length || 0);
    },
    openKudosList(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent(`open-reaction-drawer-selected-tab-${this.activity.id}`, {detail: {
        activityId: this.activity.id,
        tab: 'kudos',
        activityType: 'ACTIVITY'
      }}));
    },
  }
};
</script>