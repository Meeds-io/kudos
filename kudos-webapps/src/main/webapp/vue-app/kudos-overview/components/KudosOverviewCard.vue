<template>
  <v-row
    :class="clickable && 'clickable' || ''"
    class="border-box-sizing text-center justify-center"
    @click="clickable && $emit('open-drawer')">
    <v-col class="my-auto px-0 col-md-3 kudosOverviewIcon">
      <v-icon class="tertiary-color uiIconAward" :size="isOverviewDisplay ? '40' : '56'" />
    </v-col>
    <v-col class="my-auto col-md-2 kudosOverviewCount text-color mx-0 display-1 font-weight-bold">
      <slot name="count"></slot>
    </v-col>
    <v-col class="my-auto col-12 col-md-1 text-md-left px-0 mx-0 kudosOverviewLabel text-color">
      <slot name="label"></slot>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: {
    clickable: {
      type: Boolean,
      default: null,
    },
    isOverviewDisplay: {
      type: Boolean,
      default: () => false,
    },
  },
};
</script>