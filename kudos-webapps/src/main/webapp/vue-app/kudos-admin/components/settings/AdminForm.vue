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
  <v-card flat>
    <div class="text-header mt-4 mb-2">
      {{ $t('kudos.administration.label') }}
    </div>
    <div class="d-flex flex-row kudosPeriodConfiguration">
      <div class="flex-grow-1 flex-shrink-1">
        <v-text-field
          v-model="kudosPerPeriod"
          type="number"
          name="kudosPerPeriod"
          class="pa-0"
          hide-details
          dense
          outlined
          required />
      </div>
      <div class="flex-grow-0 flex-shrink-0 ma-auto">
        <span class="mx-2"> {{ $t('exoplatform.kudos.label.kudosPer') }} </span>
      </div>
      <div class="flex-grow-1 flex-shrink-1">
        <select
          id="applicationToolbarFilterSelect"
          v-model="kudosPeriodType"
          class="ignore-vuetify-classes my-auto">
          <option
            v-for="item in periods"
            :key="item.value"
            :value="item.value">
            {{ item.text }}
          </option>
        </select>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    loading: false,
    kudosPeriodType: 'WEEK',
    kudosPerPeriod: 3,
    originalSettings: [3, 'WEEK'],
  }),
  computed: {
    periods(){
      return [
        {
          text: this.$t('exoplatform.kudos.label.week'),
          value: 'WEEK'
        },
        {
          text: this.$t('exoplatform.kudos.label.month'),
          value: 'MONTH'
        },
        {
          text: this.$t('exoplatform.kudos.label.quarter'),
          value: 'QUARTER'
        },
        {
          text: this.$t('exoplatform.kudos.label.semester'),
          value: 'SEMESTER'
        },
        {
          text: this.$t('exoplatform.kudos.label.year'),
          value: 'YEAR'
        }
      ];
    },
    modified() {
      return JSON.stringify(this.originalSettings) !== JSON.stringify([this.kudosPerPeriod, this.kudosPeriodType]);
    },
  },
  watch: {
    loading() {
      this.$emit('loading', this.loading);
    },
    modified() {
      this.$emit('modified', this.modified);
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      return this.$kudosSettings.initSettings()
        .then(() => {
          this.kudosPerPeriod = window.kudosSettings && window.kudosSettings.kudosPerPeriod;
          this.kudosPeriodType = window.kudosSettings && window.kudosSettings.kudosPeriodType;
          this.originalSettings = [this.kudosPerPeriod, this.kudosPeriodType];
        });
    },
    reset() {
      this.kudosPerPeriod = 3;
      this.kudosPeriodType = 'WEEK';
    },
    save() {
      this.loading = true;
      this.error = null;
      this.$kudosSettings.saveSettings({
        kudosPerPeriod: this.kudosPerPeriod,
        kudosPeriodType: this.kudosPeriodType
      })
        .then(() => this.$emit('saved'))
        .finally(() => this.loading = false);
    }
  }
};
</script>