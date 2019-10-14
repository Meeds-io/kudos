<template>
  <v-app
    id="KudosAdminApp"
    color="transaprent"
    class="VuetifyApp">
    <main>
      <v-layout>
        <v-flex class="white text-center" flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-tabs v-model="selectedTab" grow>
            <v-tabs-slider color="primary" />
            <v-tab key="general" href="#general">{{ $t('exoplatform.kudos.label.settings') }}</v-tab>
            <v-tab key="kudosList" href="#kudosList">{{ $t('exoplatform.kudos.label.kudosList') }}</v-tab>
          </v-tabs>

          <v-tabs-items v-model="selectedTab">
            <v-tab-item
              id="general"
              value="general"
              eager>
              <v-progress-circular
                v-if="loading"
                indeterminate
                color="white"
                class="mr-2" />
              <v-card v-else flat>
                <v-card-text>
                  <div class="text-left kudosPeriodConfiguration">
                    <div class="ma-auto">
                      <v-text-field
                        v-model="kudosPerPeriod"
                        :label="$t('exoplatform.kudos.label.numberOfKudos')"
                        type="number"
                        name="kudosPerPeriod"
                        required />
                    </div>
                    <div class="ma-auto">
                      <span class="ml-2 mr-2"> {{ $t('exoplatform.kudos.label.kudosPer') }} </span>
                    </div>
                    <div class="ma-auto">
                      <v-combobox
                        v-model="kudosPeriodType"
                        :items="periods"
                        :return-object="false"
                        :label="$t('exoplatform.kudos.label.periodType')"
                        hide-no-data
                        hide-selected
                        small-chips>
                        <!-- Without slot-scope, the template isn't displayed -->
                        <!-- eslint-disable-next-line vue/no-unused-vars -->
                        <template slot="selection" slot-scope="data">
                          {{ selectedPeriodTypeText }}
                        </template>
                      </v-combobox>
                    </div>
                  </div>
                  <v-flex
                    id="accessPermissionAutoComplete"
                    class="contactAutoComplete mt-4">
                    <v-autocomplete
                      ref="accessPermissionAutoComplete"
                      v-model="accessPermission"
                      :items="accessPermissionOptions"
                      :loading="isLoadingSuggestions"
                      :label="$t('exoplatform.kudos.label.kudosAccessPermission')"
                      :placeholder="$t('exoplatform.kudos.label.kudosAccessPermissionPlaceholder')"
                      attach="#accessPermissionAutoComplete"
                      class="contactAutoComplete"
                      content-class="contactAutoCompleteContent"
                      max-width="100%"
                      item-text="name"
                      item-value="id"
                      hide-details
                      hide-selected
                      chips
                      cache-items
                      dense
                      flat
                      @update:search-input="search">
                      <template slot="no-data">
                        <v-list-item>
                          <v-list-item-title>
                            {{ $t('exoplatform.kudos.label.kudosAccessPermissionNoData') }}
                          </v-list-item-title>
                        </v-list-item>
                      </template>
                      <template slot="selection" slot-scope="{ item, selected }">
                        <v-chip
                          v-if="item.error"
                          :input-value="selected"
                          class="autocompleteSelectedItem">
                          <del><span>{{ item.name }}</span></del>
                        </v-chip>
                        <v-chip
                          v-else
                          :input-value="selected"
                          class="autocompleteSelectedItem">
                          <span>{{ item.name }}</span>
                        </v-chip>
                      </template>
                      <!-- Without slot-scope, the template isn't displayed -->
                      <!-- eslint-disable-next-line vue/no-unused-vars -->
                      <template slot="item" slot-scope="{ item }">
                        <v-list-item-avatar
                          v-if="item.avatar"
                          size="20">
                          <img :src="item.avatar">
                        </v-list-item-avatar>
                        <v-list-item-title class="text-left" v-text="item.name" />
                      </template>
                    </v-autocomplete>
                  </v-flex>
                </v-card-text>
                <v-card-actions>
                  <v-spacer />
                  <div class="ignore-vuetify-classes">
                    <button class="btn btn-primary ignore-vuetify-classes mb-3" @click="saveGlobalSettings">
                      {{ $t('exoplatform.kudos.button.save') }}
                    </button>
                  </div>
                  <v-spacer />
                </v-card-actions>
              </v-card>
            </v-tab-item>
            <v-tab-item
              id="kudosList"
              value="kudosList"
              class="text-center"
              eager>
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
      selectedTab: 'general',
      error: null,
      accessPermission: null,
      accessPermissionOptions: [],
      accessPermissionSearchTerm: null,
      kudosPeriodType: null,
      isLoadingSuggestions: false
    };
  },
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
      ]
    },
    selectedPeriodTypeText() {
      let selectedPeriodType = this.periods.find(period => period.value === this.kudosPeriodType);
      selectedPeriodType = selectedPeriodType ? selectedPeriodType.value : this.kudosPeriodType;
      if (selectedPeriodType) {
        return this.$t(`exoplatform.kudos.label.${selectedPeriodType.toLowerCase()}`)
      }
      return '';
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
    search(value) {
      this.accessPermissionSearchTerm = value;
    },
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
            throw new Error(this.$t('exoplatform.kudos.error.errorSavingKudosSettings'));
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