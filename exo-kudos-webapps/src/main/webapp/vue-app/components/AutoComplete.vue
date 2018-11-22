<template>
  <v-flex :id="id" class="contactAutoComplete">
    <v-autocomplete ref="selectAutoComplete"
                    v-model="selectedValue"
                    :items="items"
                    :loading="isLoadingSuggestions"
                    :search-input.sync="searchTerm"
                    :label="inputLabel"
                    :disabled="disabled"
                    :attach="`#${id}`"
                    :placeholder="inputPlaceholder"
                    class="contactAutoComplete"
                    content-class="contactAutoCompleteContent"
                    max-width="100%"
                    item-text="name"
                    item-value="id_type"
                    hide-details
                    hide-selected
                    chips
                    cache-items
                    dense
                    flat>
  
      <template slot="no-data">
        <v-list-tile>
          <v-list-tile-title>
            Search for a space or user
          </v-list-tile-title>
        </v-list-tile>
      </template>
  
      <template slot="selection" slot-scope="{ item, selected }">
        <v-chip
          v-if="item.avatar"
          :selected="selected"
          class="autocompleteSelectedItem"
          @input="selectItem(item)">
          <span>{{ item.name }}</span>
        </v-chip>
        <v-label v-else :selected="selected" class="black--text" solo @input="selectItem(item)">
          {{ item.name }}
        </v-label>
      </template>

      <template slot="item" slot-scope="{ item, tile }">
        <v-list-tile-avatar v-if="item.avatar" tile size="20">
          <img :src="item.avatar">
        </v-list-tile-avatar>
        <v-list-tile-title v-text="item.name" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
import {searchContact, searchUserOrSpaceObject} from '../js/KudosIdentity.js';

export default {
  props: {
    inputLabel: {
      type: String,
      default: function() {
        return null;
      }
    },
    inputPlaceholder: {
      type: String,
      default: function() {
        return null;
      }
    },
    disabled: {
      type: Boolean,
      default: function() {
        return false;
      }
    }
  },
  data () {
    return {
      items: [],
      id: `AutoComplete${parseInt(Math.random() * 10000).toString().toString()}`,
      selectedValue: null,
      searchTerm: null,
      isLoadingSuggestions: false,
      error: null
    };
  },
  watch: {
    searchTerm(value) {
      if (value && value.length) {
        this.isLoadingSuggestions = true;
        try {
          searchContact(value)
            .then(data => {
              this.items = data;
            });
        } finally {
          this.isLoadingSuggestions = false;
        }
      } else {
        this.items = [];
      }
    },
    selectedValue() {
      this.$refs.selectAutoComplete.isFocused = false;
      if (this.selectedValue) {
        const type = this.selectedValue.substring(0, this.selectedValue.indexOf('_'));
        const id = this.selectedValue.substring(this.selectedValue.indexOf('_') + 1);
        this.$emit("item-selected", {id: id, type: type, id_type: this.selectedValue});
      }
    }
  },
  methods: {
    clear() {
      this.items = [];
      this.selectedValue = null;
      this.searchTerm = null;
      this.isLoadingSuggestions = false;
      this.error = null;
    },
    selectItem(id, type) {
      if (type) {
        searchUserOrSpaceObject(id, type)
          .then(item => {
            if (item) {
              item.id_type = `${item.type}_${item.id}`;
              this.items.push(item);
              this.$refs.selectAutoComplete.selectItem(item);
            }
          });
      } else {
        const item = {id_type: id, name: id};
        this.items.push(item);
        this.$refs.selectAutoComplete.selectItem(item);
      }
    }
  }
};
</script>

