<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association contact@meeds.io
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
  <div>
    <v-row
      id="kudosOverviewCardsParent"
      class="white border-box-sizing ma-0 align-center">
      <v-col class="kudosOverviewCard">
        <kudos-overview-card
          :clickable="owner && receivedKudosCount > 0"
          class="kudosReceivedOverviewPeriod mx-n4"
          @open-drawer="openDrawer('received')">
          <template slot="count">
            {{ receivedKudosCount || '0' }}
          </template>
          <template slot="label">
            {{ $t('exoplatform.kudos.label.received') }}
          </template> 
        </kudos-overview-card>
      </v-col>
      <v-divider
        class="my-9 mx-8 me-md-1 ms-md-5"
        vertical />
      <v-col class="kudosOverviewCard">
        <kudos-overview-card
          :clickable="owner && sentKudosCount > 0"
          class="kudosSentOverviewPeriod mx-n4"
          @open-drawer="openDrawer('sent')">
          <template slot="count">
            {{ sentKudosCount || '0' }}
          </template>
          <template slot="label">
            {{ $t('exoplatform.kudos.label.sent') }}
          </template>
        </kudos-overview-card>
      </v-col>
    </v-row>
    <kudos-overview-drawer
      v-if="owner"
      ref="kudosOverviewDrawer" />
  </div>
</template>
<script>
import {getKudosSent, getKudosReceived} from '../../js/Kudos.js'; 
export default {
  props: {
    isOwner: {
      type: Boolean,
      default: false
    }, 
    periodType: {
      type: String,
      default: ''
    },
    isOverviewDisplay: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    identityId: eXo.env.portal.profileOwnerIdentityId,
    sentKudosCount: 0,
    receivedKudosCount: 0,
    sentKudos: [],
    receivedKudos: [],
  }),
  computed: {
    owner() {
      return this.isOwner || eXo.env.portal.profileOwner === eXo.env.portal.userName;
    }
  },
  watch: {
    periodType() {
      this.refresh();
    }
  },
  created() {
    this.refresh();
  },
  methods: {
    openDrawer(kudosType) {
      if (this.owner) {
        const title = kudosType === 'sent' ?
          this.$t('exoplatform.kudos.button.sentKudos'):
          this.$t('exoplatform.kudos.button.receivedKudos');
        this.$refs.kudosOverviewDrawer.open(title, kudosType, this.identityId, this.periodType);
      }
    },
    refresh() {
      const dateInSeconds = parseInt(Date.now() / 1000);
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      getKudosSent(this.identityId, 0, true, this.periodType, dateInSeconds)
        .then(kudosList => {
          this.sentKudosCount = kudosList && kudosList.size || 0;
          this.sentKudos = kudosList && kudosList.kudos || [];
        })
        .then(() => getKudosReceived(this.identityId, 0, true, this.periodType, dateInSeconds))
        .then(kudosList => {
          this.receivedKudosCount = kudosList && kudosList.size || 0;
          this.receivedKudos = kudosList && kudosList.kudos || [];
          return this.$nextTick();
        })
        .finally(() => {
          this.$root.$emit('application-loaded');
          // Decrement 'loading' effect in top of the page
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          document.dispatchEvent(new CustomEvent('kudosCount', {detail: this.sentKudosCount + this.receivedKudosCount}));
        });
    },
  },
};
</script>