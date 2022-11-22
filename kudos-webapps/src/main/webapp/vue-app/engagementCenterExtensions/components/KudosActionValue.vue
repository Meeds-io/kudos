<template>
  <v-tooltip bottom>
    <template #activator="{ on }">
      <div v-on="on">
        <a class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid text-truncate" :href="actionURL">
          <v-icon size="15" class="primary--text">fa-award</v-icon>
          <div class="ps-2 text-truncate">{{ actionLabel }}
          </div>
        </a>
      </div>
    </template>
    <span>{{ actionLabel }}</span>
  </v-tooltip>
</template>

<script>
export default {
  props: {
    actionLabel: {
      type: String,
      default: null
    },
    actionURL: {
      type: String,
      default: null
    },
  },
};
</script>