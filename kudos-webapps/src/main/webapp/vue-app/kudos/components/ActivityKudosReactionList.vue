<template>
  <div v-if="kudosList.length" class="kudos-list">
    <activity-kudos-reaction-item
      v-for="(kudos , i) in kudosList"
      :key="i"
      :kudos="kudos"
      class="pl-3 pt-2 pb-1" />
  </div>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    }
  },
  data () {
    return {
      kudosList: [],
      limit: 10,
    };
  },
  created() {
    this.retrieveKudos();
  },
  methods: {
    retrieveKudos() {
      return this.$kudosService.getEntityKudos('ACTIVITY', this.activityId).then(data => {
        this.kudosList = data;
        document.dispatchEvent(new CustomEvent('update-reaction-extension', {
          detail: {
            numberOfReactions: this.kudosList.length,
            type: 'kudos'
          }
        }));
      })
        .catch((e => {
          console.error('error retrieving activity kudos' , e) ;
        }));
    },
  },
};
</script>
