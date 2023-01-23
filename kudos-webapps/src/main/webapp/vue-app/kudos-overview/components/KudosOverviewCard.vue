<template>
  <div
    :class="clickable && 'clickable' || ''"
    class="border-box-sizing text-center justify-center align-center d-flex flex-wrap"
    @click="clickable && $emit('open-drawer')">
    <div class="px-0 kudosOverviewIcon pa-0">
      <v-icon class="tertiary-color uiIconAward" size="40" />
    </div>
    <div class="kudosOverviewCount text-color mx-2 display-1 font-weight-bold pa-0 d-flex align-center">
      <slot name="count"></slot>
    </div>
    <div class="px-0 mx-0">
      <v-card
        :min-width="55"
        class="d-flex align-center flex-no-wrap"
        flat>
        <slot name="label"></slot>
      </v-card>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    clickable: {
      type: Boolean,
      default: null,
    },
  },
};
</script>