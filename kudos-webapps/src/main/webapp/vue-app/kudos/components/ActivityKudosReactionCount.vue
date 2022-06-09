<template>
  <a
    v-if="kudosNumber>0"
    class="my-1 me-2 KudosNumber"
    @click="openKudosList">
    {{ kudosNumber }} Kudos</a>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    kudosNumber: 0,
  }),
  created() {
    this.$root.$on('kudos-refresh-comment', this.getKudosCount);
    this.getKudosCount();
  },
  methods: {
    getKudosCount() {
      return this.$kudosService.computeActivityKudosList(this.activity)
        .then(() => this.kudosNumber = this.activity.linkedKudosList.length || 0);
    },
    openKudosList(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent(`open-reaction-drawer-selected-tab-${this.activity.id}`, {detail: {
        activityId: this.activity.id,
        tab: 'kudos',
        activityType: 'ACTIVITY'
      }}));
    },
  }
};
</script>