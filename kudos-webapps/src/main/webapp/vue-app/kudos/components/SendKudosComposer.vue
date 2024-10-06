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
  <v-card
    id="sendKudosComposerButton"
    class="mx-4 mb-3 px-6 py-3 card-border-radius"
    outlined 
    flat 
    hover>
    <div
      class="d-flex flex-row align-center" 
      @click="openSendKudosDrawer">
      <v-btn
        class="px-auto"
        width="44px"
        height="50px"
        icon>
        <v-icon
          color="primary"
          size="50px">
          fa-award
        </v-icon>
      </v-btn>
      <span class="font-weight-bold ms-5">
        {{ $t('exoplatform.kudos.title.sendAKudos') }}
      </span>
    </div>
  </v-card>
</template>
<script>
export default {
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('activity-composer-closed'));
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        parentId: '',
        owner: eXo.env.portal.userName,
        spaceURL: eXo.env.portal.spaceUrl,
        readOnlySpace: eXo.env.portal.spaceUrl ? true : false
      }}));
    },
  },
};
</script> 