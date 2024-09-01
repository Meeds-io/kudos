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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="kudosOverviewDrawer"
    right>
    <template #title>
      {{ $t('kudosOverview.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-tabs
        v-model="tabName"
        slider-size="4">
        <v-tab
          tab-value="sent"
          href="#sent">
          {{ $t('kudosOverview.tab.sent') }}
        </v-tab>
        <v-tab
          tab-value="received"
          href="#received">
          {{ $t('kudosOverview.tab.received') }}
        </v-tab>
      </v-tabs>
      <v-tabs-items
        v-model="tabName"
        class="px-4">
        <v-tab-item
          v-show="!loading"
          value="sent">
          <kudos-overview-item-list
            v-if="tabName === 'sent'"
            :identity-id="identityId"
            :limit="limit"
            kudos-type="sent"
            @has-more="hasMore = $event"
            @loading="loading = $event" />
        </v-tab-item>
        <v-tab-item
          v-show="!loading"
          value="received">
          <kudos-overview-item-list
            v-if="tabName === 'received'"
            :identity-id="identityId"
            :limit="limit"
            kudos-type="received"
            @has-more="hasMore = $event"
            @loading="loading = $event" />
        </v-tab-item>
      </v-tabs-items>
    </template>
    <template v-if="hasMore" slot="footer">
      <v-spacer />
      <v-btn
        :loading="loading"
        :disabled="loading"
        class="loadMoreButton ma-auto btn"
        block
        @click="loadMore">
        {{ $t('exoplatform.kudos.button.showMore') }}
      </v-btn>
      <v-spacer />
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    identityId: null,
    tabName: null,
    drawer: false,
    loading: false,
    hasMore: false,
    limit: 20,
  }),
  created() {
    this.$root.$on('kudos-overview-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('kudos-overview-drawer', this.open);
  },
  methods: {
    open(kudosType, identityId, periodType) {
      this.tabName = kudosType;
      this.identityId = identityId;
      this.periodType = periodType;
      this.limit = 20;
      this.$refs.drawer.open();
    },
    loadMore() {
      this.limit += 20;
    },
  }
};
</script>