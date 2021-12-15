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
      this.updateKudosList(event);
    });
    document.addEventListener(`check-reactions-${this.activityId}`, this.updateKudos);
  },
  watch: {
    activityType() {
      if (this.activityType === 'COMMENT') {
        this.activityId = this.activityId.replace('comment','');
      }
      this.retrieveKudos();
    }
  },
  computed: {
    sortedKudosList() {
      return this.kudosList.slice().sort((kudos1, kudos2) => {
        return kudos2.timeInSeconds - kudos2.timeInSeconds;
      });
    },
    kudosActivityId() {
      return this.activityType === 'COMMENT' ? this.activityId.replace('comment','') : this.activityId;
    }
  },
  methods: {
    retrieveKudos() {
      return this.$kudosService.getEntityKudos(this.activityType, this.activityId).then(data => {
        this.kudosList = data;
        this.updateKudos();
      })
        .catch((e => {
          console.error('error retrieving activity kudos' , e) ;
        }));
    },
    updateKudosList(event) {
      if (event && event.detail) {
        if (this.activityId === event.detail.entityId) {
          const kudosReceived = event.detail;
          this.kudosList.push(kudosReceived);
          this.updateKudos();
        }
      }
    },
    updateKudos() {
      document.dispatchEvent(new CustomEvent(`update-reaction-extension-${this.parentId}`, {
        detail: {
          numberOfReactions: this.kudosList.length,
          type: 'kudos'
        }
      }));
    }
  },
};
</script>
