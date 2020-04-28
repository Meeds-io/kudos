<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <exo-drawer
    ref="kudosOverviewDrawer"
    class="kudosOverviewDrawer"
    right>
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-list v-if="kudosList">
        <kudos-overview-item
          v-for="kudos in kudosList"
          :key="kudos.technicalId"
          :kudos-item="kudos"
          class="border-color border-radius" />
      </v-list>
    </template>
    <template v-if="hasMore" slot="footer">
      <v-spacer />
      <v-btn
        :loading="loading"
        :disabled="loading"
        class="loadMoreButton ma-auto btn"
        block
        @click="loadNextPage">
        {{ $t('exoplatform.kudos.button.showMore') }}
      </v-btn>
      <v-spacer />
    </template>
  </exo-drawer>
</template>

<script>
import {getKudosSent, getKudosReceived} from '../../js/Kudos.js'; 

export default {
  data: () => ({
    title: '',
    kudosType: null,
    identityId: null,
    kudosRetrievalMethod: null,
    pageSize: 20,
    limit: 20,
    size: 0,
    kudosList: [],
  }),
  computed: {
    hasMore() {
      return this.size > this.limit;
    },
  },
  methods: {
    reset() {
      this.limit = 20;
      this.size = 0;
      this.kudosList = [];
    },
    open(title, kudosType, identityId, periodType) {
      this.title = title;
      this.identityId = identityId;
      this.periodType = periodType;
      this.kudosType = kudosType;
      this.kudosRetrievalMethod = kudosType === 'sent' && getKudosSent || getKudosReceived;

      this.reset();
      this.retrieveList();
      this.$refs.kudosOverviewDrawer.open();
    },
    loadMore() {
      this.limit += this.pageSize;
      this.retrieveList();
    },
    retrieveList() {
      const dateInSeconds = parseInt(Date.now() / 1000);
      this.$refs.kudosOverviewDrawer.startLoading();
      return this.kudosRetrievalMethod(this.identityId, this.limit, true, this.periodType, dateInSeconds)
        .then(data => {
          this.size = data && data.size || 0;
          this.kudosList = data && data.kudos || [];
        })
        .finally(() => {
          this.$refs.kudosOverviewDrawer.endLoading();
        });
    },
  }
};
</script>