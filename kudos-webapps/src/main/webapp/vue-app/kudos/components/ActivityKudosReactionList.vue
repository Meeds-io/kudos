<template>
  <div v-if="kudosList.length" class="kudos-list">
    <activity-kudos-reaction-item
      v-for="(kudos , i) in kudosList"
      :key="i"
      :kudos="kudos"
      class="pl-3 pt-2 pb-1" />
  </div>
  <activity-kudos-reaction-empty-list
    v-else
    :activity-poster-id="activityPosterId"
    :activity-id="activityId" />
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    },
    activityType: {
      type: String,
      default: () => ''
    },
    activityPosterId: {
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
  watch: {
    activityId() {
      this.retrieveKudos();
    }
  },
  methods: {
    retrieveKudos() {
      return this.$kudosService.getEntityKudos(this.activityType, this.activityId).then(data => {
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
    openKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.activityId,
        parentId: '',
        type: 'ACTIVITY',
      }}));
    }
  },
};
</script>
