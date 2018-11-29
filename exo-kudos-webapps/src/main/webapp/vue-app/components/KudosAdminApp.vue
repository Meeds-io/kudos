<template>
  <v-app id="KudosAdminApp" color="transaprent">
    <main>
      <v-layout>
        <v-flex class="white text-xs-center" flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-tabs v-model="selectedTab" grow>
            <v-tabs-slider color="primary" />
            <v-tab key="general">Settings</v-tab>
            <v-tab key="kudosList">Kudos list</v-tab>
          </v-tabs>

          <v-tabs-items v-model="selectedTab">
            <v-tab-item id="general">
              <v-progress-circular v-if="loading" indeterminate color="white" class="mr-2"></v-progress-circular>
              <v-card v-else flat>
                <v-card-text>
                  <div class="text-xs-left kudosPeriodConfiguration">
                    <v-text-field
                      v-model="kudosPerPeriod"
                      :rules="mandatoryRule"
                      label="Number of Kudos"
                      type="number"
                      name="kudosPerPeriod" />
                    <span class="ml-2 mr-2"> kudos per </span>
                    <v-combobox
                      v-model="kudosPeriodType"
                      :items="periods"
                      :return-object="false"
                      label="Period type"
                      hide-no-data
                      hide-selected
                      small-chips>
                      <template slot="selection" slot-scope="data">
                        {{ selectedPeriodType }}
                      </template>
                    </v-combobox>
                  </div>
                  <v-flex
                    id="accessPermissionAutoComplete"
                    class="contactAutoComplete mt-4">
                    <v-autocomplete
                      ref="accessPermissionAutoComplete"
                      v-model="accessPermission"
                      :items="accessPermissionOptions"
                      :loading="isLoadingSuggestions"
                      :search-input.sync="accessPermissionSearchTerm"
                      attach="#accessPermissionAutoComplete"
                      label="Kudos access permission (Spaces only)"
                      class="contactAutoComplete"
                      placeholder="Start typing to Search a space"
                      content-class="contactAutoCompleteContent"
                      max-width="100%"
                      item-text="name"
                      item-value="id"
                      hide-details
                      hide-selected
                      chips
                      cache-items
                      dense
                      flat>
                      <template slot="no-data">
                        <v-list-tile>
                          <v-list-tile-title>
                            Search for a <strong>Space</strong>
                          </v-list-tile-title>
                        </v-list-tile>
                      </template>
                      <template slot="selection" slot-scope="{ item, selected }">
                        <v-chip v-if="item.error" :selected="selected" class="autocompleteSelectedItem">
                          <del><span>{{ item.name }}</span></del>
                        </v-chip>
                        <v-chip v-else :selected="selected" class="autocompleteSelectedItem">
                          <span>{{ item.name }}</span>
                        </v-chip>
                      </template>
                      <template slot="item" slot-scope="{ item, tile }">
                        <v-list-tile-avatar v-if="item.avatar" tile size="20">
                          <img :src="item.avatar">
                        </v-list-tile-avatar>
                        <v-list-tile-title v-text="item.name" />
                      </template>
                    </v-autocomplete>
                  </v-flex>
                </v-card-text>
                <v-card-actions>
                  <v-spacer />
                  <button class="btn btn-primary mb-3" @click="saveGlobalSettings">
                    Save
                  </button>
                  <v-spacer />
                </v-card-actions>
              </v-card>
            </v-tab-item>
            <v-tab-item id="kudosList" class="text-xs-center">
              <kudos-list
                :default-kudos-period-type="kudosPeriodType" />
            </v-tab-item>
          </v-tabs-items>
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import KudosList from './KudosList.vue';

import {initSettings, saveSettings} from '../js/KudosSettings.js';
import {searchSpaces} from '../js/KudosIdentity.js';

export default {
  components: {
    KudosList
  },
  data: vm => {
    return {
      loading: false,
      selectedTab: true,
      error: null,
      accessPermission: null,
      accessPermissionOptions: [],
      accessPermissionSearchTerm: null,
      kudosPeriodType: null,
      isLoadingSuggestions: false,
      periods: [
        {
          text: 'Week',
          value: 'WEEK'
        },
        {
          text: 'Month',
          value: 'MONTH'
        },
        {
          text: 'Quarter',
          value: 'QUARTER'
        },
        {
          text: 'Semester',
          value: 'SEMESTER'
        },
        {
          text: 'Year',
          value: 'YEAR'
        }
      ],
      mandatoryRule: [
        (v) => !!v || 'Field is required'
      ]
    };
  },
  computed: {
    selectedPeriodType() {
      const selectedPeriodType = this.periods.find(period => period.value === this.kudosPeriodType);
      return selectedPeriodType ? selectedPeriodType.text : this.kudosPeriodType;
    }
  },
  watch: {
    accessPermissionSearchTerm() {
      this.isLoadingSuggestions = true;
      searchSpaces(this.accessPermissionSearchTerm)
        .then(items => {
          if (items) {
            this.accessPermissionOptions = items;
          } else {
            this.accessPermissionOptions = [];
          }
          this.isLoadingSuggestions = false;
        })
        .catch((e) => {
          console.debug("searchSpaces method - error", e);
          this.isLoadingSuggestions = false;
        });
    },
    accessPermission(newValue, oldValue) {
      if (oldValue) {
        this.accessPermissionSearchTerm = null;
        // A hack to close on select
        // See https://www.reddit.com/r/vuetifyjs/comments/819h8u/how_to_close_a_multiple_autocomplete_vselect/
        this.$refs.accessPermissionAutoComplete.isFocused = false;
      }
    }
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      return initSettings()
        .then(() => {
          this.accessPermission = window.kudosSettings && window.kudosSettings.accessPermission;
          this.kudosPerPeriod = window.kudosSettings && window.kudosSettings.kudosPerPeriod;
          this.kudosPeriodType = window.kudosSettings && window.kudosSettings.kudosPeriodType;
          if (this.accessPermission) {
            searchSpaces(this.accessPermission)
              .then(items => {
                if (items) {
                  this.accessPermissionOptions = items;
                } else {
                  this.accessPermissionOptions = [];
                }
                if (!this.accessPermissionOptions.find(item => item.id === this.accessPermission)) {
                  this.accessPermissionOptions.push({id : this.accessPermission, name : this.accessPermission, error : true});
                }
              });
          }
        })
        .catch(e => {
          this.error = e;
        });
    },
    saveGlobalSettings() {
      this.loading = true;
      this.error = null;
      saveSettings({
        accessPermission: this.accessPermission,
        kudosPerPeriod: this.kudosPerPeriod,
        kudosPeriodType: this.kudosPeriodType
      })
        .then(status => {
          if(!status) {
            throw new Error("Error sending Kudo, please contact your administrator.");
          }
          return this.init();
        })
        .catch(e => {
          console.debug("Error saving kudo", e);
          this.error = String(e);
          throw e;
        })
        .finally(() => {
          this.loading = false;
        });
    }
  }
};
</script>