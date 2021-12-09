<template>
  <div v-if="sortedKudosList.length" class="kudos-list">
    <activity-kudos-reaction-item
      v-for="(kudos , i) in sortedKudosList"
      :key="i"
      :kudos="kudos"
      class="pl-3 pt-2 pb-1" />
  </div>
  <activity-kudos-reaction-empty-list
    v-else
    :activity-poster-id="activityPosterId"
    :activity-type="activityType"
    :parent-id="parentId"
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
    parentId: {
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
    document.addEventListener('exo-kudos-sent',(event) => {
      if (event && event.detail) {
        this.retrieveKudos();
      }
    });
  },
  computed: {
    sortedKudosList() {
      return this.kudosList.slice().sort((kudos1, kudos2) => {
        return kudos2.timeInSeconds - kudos2.timeInSeconds;
      });
    }
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
  },
};
</script>
