<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-list v-if="size">
    <kudos-overview-item
      v-for="kudos in kudosList"
      :key="kudos.technicalId"
      :kudos-item="kudos" />
  </v-list>
  <v-card
    v-else-if="!loading"
    class="d-flex flex-column align-center justify-center ma-8"
    flat>
    <v-icon color="tertiary" size="60">fa-award</v-icon>
    <span v-if="kudosType === 'received' || !isOwner" class="mt-4">{{ placeholder }}</span>
    <span
      v-else
      v-html="emptyKudosSummaryText"
      class="mt-5"></span>
  </v-card>
</template>
<script>
import {getKudosSent, getKudosReceived} from '../../../js/Kudos.js';

export default {
  props: {
    identityId: {
      type: String,
      default: null,
    },
    kudosType: {
      type: String,
      default: null,
    },
    limit: {
      type: Number,
      default: () => 20,
    },
  },
  data: () => ({
    isOwner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    emptyKudosActionName: 'kudos-check-actions',
    kudosList: [],
    pageSize: 20,
    loading: false,
    size: 0,
  }),
  computed: {
    placeholder() {
      return this.kudosType === 'sent'
        && this.$t('kudosOverview.noKudosSentYet')
        || this.$t('kudosOverview.noKudosReceivedYet');
    },
    emptyKudosSummaryText() {
      return this.$t('gamification.overview.emptyKudosMessage', {
        0: !this.isExternal && `<a class="primary--text font-weight-bold" href="javascript:void(0)" onclick="document.dispatchEvent(new CustomEvent('${this.emptyKudosActionName}'))">` || '',
        1: !this.isExternal && '</a>' || '',
      });
    },
    kudosRetrievalMethod() {
      return this.kudosType === 'sent' && getKudosSent || getKudosReceived;
    },
    hasMore() {
      return this.size > this.limit;
    },
  },
  watch: {
    kudosType() {
      if (!this.loading) {
        this.retrieveList();
      }
    },
    limit() {
      if (!this.loading) {
        this.retrieveList();
      }
    },
    hasMore: {
      immediate: true,
      handler() {
        this.$emit('has-more', this.hasMore);
      },
    },
    loading: {
      immediate: true,
      handler() {
        this.$emit('loading', this.loading);
      },
    },
  },
  created() {
    this.retrieveList();
    document.addEventListener(this.emptyKudosActionName, this.clickOnKudosEmptyActionLink);
    document.addEventListener('exo-kudos-sent', this.retrieveList);
  },
  beforeDestroy() {
    document.removeEventListener(this.emptyKudosActionName, this.clickOnKudosEmptyActionLink);
    document.removeEventListener('exo-kudos-sent', this.retrieveList);
  },
  methods: {
    async retrieveList() {
      const dateInSeconds = parseInt(Date.now() / 1000);
      this.loading = true;
      try {
        const data = await this.kudosRetrievalMethod(this.identityId, this.limit, true, this.periodType, dateInSeconds);
        this.size = data && data.size || 0;
        this.kudosList = data && data.kudos || [];
      } finally {
        this.loading = false;
      }
    },
    clickOnKudosEmptyActionLink() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
      }}));
    },
  }
};
</script>