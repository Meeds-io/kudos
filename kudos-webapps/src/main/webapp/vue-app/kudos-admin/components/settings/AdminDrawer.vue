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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    right>
    <template #title>
      {{ $t('kudos.administration.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-5">
        <span class="mb-2">
          {{ $t('kudos.administration.introduction1') }}
        </span>
        <span class="mb-2">
          {{ $t('kudos.administration.introduction2') }}
        </span>
        <span class="mb-2">
          {{ $t('kudos.administration.introduction3') }}
        </span>
        <v-divider />
        <kudos-admin-form
          ref="adminForm"
          @modified="modified = $event"
          @loading="loading = $event"
          @saved="close" />
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          :disabled="loading"
          class="ps-0"
          color="primary"
          text
          @click="$refs.adminForm.reset()">
          <v-icon class="pe-1">fa-redo</v-icon>
          {{ $t('kudos.administration.reset') }}
        </v-btn>
        <v-spacer />
        <v-btn
          :disabled="loading"
          class="btn me-2"
          @click="close">
          {{ $t('kudos.administration.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="loading"
          class="btn btn-primary"
          @click="$refs.adminForm.save()">
          {{ $t('kudos.administration.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    modified: false,
    loading: false,
    drawer: false,
  }),
  methods: {
    open() {
      this.modified = false;
      this.loading = false;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
      this.modified = false;
      this.loading = false;
    },
  },
};
</script>
