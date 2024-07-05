<template>
  <v-card
    class="border-box-sizing text-center justify-center align-center d-flex flex-column"
    flat
    v-on="clickable && {
      click: () => $emit('open-drawer')
    }">
    <div class="d-flex">
      <div class="ms-2 me-1 my-auto kudosOverviewIcon pa-0">
        <v-icon class="uiIconKudos height-auto tertiary-color" size="32">fa fa-award</v-icon>
      </div>
      <div class="kudosOverviewCount mx-2 text-h5 font-weight-bold pa-0 d-flex align-center">
        <slot name="count"></slot>
      </div>
    </div>
    <div class="px-0 mx-0 mt-2 d-flex justify-center">
      <slot name="label"></slot>
    </div>
  </v-card>
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