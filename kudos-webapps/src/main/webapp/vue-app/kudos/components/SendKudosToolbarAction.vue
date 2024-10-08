<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
  contact@meeds.io
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
  <v-tooltip v-if="!spaceId" bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        id="kudosBtnToolbar"
        height="36px"
        width="36px"
        icon
        v-bind="attrs"
        v-on="on"
        @click="openSendKudosDrawer">
        <v-icon
          size="20px"
          color="primary">
          fa-award
        </v-icon>
      </v-btn>
    </template>
    <span>
      {{ $t('exoplatform.kudos.tooltip') }}
    </span>
  </v-tooltip>
  <v-btn
    v-else
    id="kudosBtnToolbar"
    class="d-flex flex-row align-center py-2 px-3"
    text
    @click="openSendKudosDrawer">
    <v-icon
      size="27px"
      color="primary">
      fa-award
    </v-icon>
    <span class="font-weight-bold ms-5">
      {{ $t('kudos.title') }}
    </span>
  </v-btn>
</template>
<script>
export default {
  data () {
    return {
      spaceId: eXo.env.portal.spaceId,
    };
  },
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        spacePrettyName: eXo.env.portal.spaceName,
        readOnlySpace: !!eXo.env.portal.spaceId
      }}));
    },
  },
};
</script> 