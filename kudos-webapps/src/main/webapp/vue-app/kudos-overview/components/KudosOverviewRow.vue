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
      v-if="hasKudos"
      id="kudosOverviewCardsParent"
      class="white border-box-sizing ma-0 align-center">
      <v-col class="kudosOverviewCard col-6 pa-0">
        <kudos-overview-card
          :clickable="isOwner && receivedKudosCount > 0"
          class="kudosReceivedOverviewPeriod mx-auto"
          @open-drawer="openDrawer('received')">
          <template slot="count">
            {{ receivedKudosCount || '0' }}
          </template>
          <template slot="label">
            {{ $t('exoplatform.kudos.label.received') }}
          </template>
        </kudos-overview-card>
      </v-col>
      <v-col class="kudosOverviewCard col-6 pa-0">
        <kudos-overview-card
          :clickable="isOwner && sentKudosCount > 0"
          class="kudosSentOverviewPeriod mx-auto"
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
    <div v-else-if="!loading" class="d-flex flex-column align-center justify-center">
      <v-icon color="tertiary" size="60">fa-award</v-icon>
      <span
        class="subtitle-1 mt-3 text-wrap">
        {{ noKudosThisPeriodLabel }}
      </span>
    </div>
    <kudos-overview-drawer v-if="isOwner" />
  </div>
</template>
<script>
import {getKudosSent, getKudosReceived} from '../../js/Kudos.js'; 
export default {
  props: {
    periodType: {
      type: String,
      default: () => '',
    },
  },
  data: () => ({
    identityId: eXo.env.portal.profileOwnerIdentityId,
    sentKudosCount: 0,
    receivedKudosCount: 0,
    loading: true,
    sentKudos: [],
    receivedKudos: [],
    isOwner: eXo.env.portal.profileOwner === eXo.env.portal.userName
  }),
  computed: {
    hasKudos() {
      return this.sentKudosCount || this.receivedKudosCount;
    },
    noKudosThisPeriodLabel() {
      return this.periodType && this.$t(`gamification.overview.emptyKudosMessage.${this.periodType.toLowerCase()}`);
    },
  },
  watch: {
    periodType() {
      this.refresh();
    },
    loading() {
      this.$emit('loading', this.loading);
    },
    hasKudos() {
      this.$emit('has-kudos', this.hasKudos);
    },
  },
  created() {
    this.refresh();
    document.addEventListener('exo-kudos-sent', this.refresh);
  },
  beforeDestroy() {
    document.removeEventListener('exo-kudos-sent', this.refresh);
  },
  methods: {
    openDrawer(kudosType) {
      if (this.isOwner) {
        this.$root.$emit('kudos-overview-drawer', kudosType, this.identityId, this.periodType);
      }
    },
    refresh() {
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      this.loading = true;
      return getKudosSent(this.identityId, 0, true, this.periodType, 0)
        .then(kudosList => {
          this.sentKudosCount = kudosList && kudosList.size || 0;
          this.sentKudos = kudosList && kudosList.kudos || [];
        })
        .then(() => getKudosReceived(this.identityId, 0, true, this.periodType, 0))
        .then(kudosList => {
          this.receivedKudosCount = kudosList && kudosList.size || 0;
          this.receivedKudos = kudosList && kudosList.kudos || [];
          return this.$nextTick();
        })
        .finally(() => {
          this.loading = false;
          this.$root.$emit('application-loaded');
          // Decrement 'loading' effect in top of the page
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          document.dispatchEvent(new CustomEvent('kudosCount', {detail: this.sentKudosCount + this.receivedKudosCount}));
        });
    },
  },
};
</script>