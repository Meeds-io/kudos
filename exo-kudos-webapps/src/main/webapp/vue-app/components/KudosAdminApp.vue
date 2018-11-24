<template>
  <v-app id="KudosAdminApp" color="transaprent" flat>
    <main>
      <v-layout>
        <v-flex class="white text-xs-center" flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-card flat>
            <v-card-text>
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
              <v-text-field
                v-model="kudosPerMonth"
                :rules="mandatoryRule"
                label="Kudos per month"
                placeholder="Kudos allowed to send by a user per month"
                type="number"
                name="kudosPerMonth" />
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <button class="btn btn-primary mb-3" @click="saveGlobalSettings">
                Save
              </button>
              <v-spacer />
            </v-card-actions>
          </v-card>
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import {getSettings, saveSettings} from '../js/KudosSettings.js';
import {searchSpaces} from '../js/KudosIdentity.js';

export default {
  data() {
    return {
      loading: false,
      error: null,
      accessPermission: null,
      accessPermissionOptions: [],
      accessPermissionSearchTerm: null,
      isLoadingSuggestions: false,
      kudosPerMonth: 0,
      mandatoryRule: [
        (v) => !!v || 'Field is required'
      ]
    };
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
      return getSettings()
        .then(settings => {
          this.accessPermission = settings && settings.accessPermission;
          this.kudosPerMonth = settings && settings.kudosPerMonth;
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
        kudosPerMonth: this.kudosPerMonth
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