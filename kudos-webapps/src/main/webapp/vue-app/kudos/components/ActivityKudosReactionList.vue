<template>
  <div v-if="kudosList.length" class="kudos-list">
    <activity-kudos-reaction-item
      v-for="(kudos , i) in kudosList"
      :key="i"
      :kudos="kudos"
      class="pl-3 pt-2 pb-1" />
  </div>
  <div v-else class="flex flex-column text-sub-title align-items-center pt-12">
    <v-icon
      class="flex d-flex disabled--text pl-1"
      size="50">
      fa-award
    </v-icon>
    <span class="flex d-flex align-self-center pt-5"> {{ $t('exoplatform.kudos.info.noKudosYet') }} <a class="pl-1" @click="openKudosDrawer"> {{ $t('exoplatform.kudos.info.sendAKudos') }} </a>
    </span>
    <span class="flex d-flex align-self-center"> {{ $t('exoplatform.kudos.label.kudosMessagePurpose') }}</span>
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
      return this.$kudosService.getEntityKudos('ACTIVITY',this.activityId).then(data => {
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
    openKudosDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.activityId,
        parentId: '',
        type: 'ACTIVITY',
      }}));
    }
  },
};
</script>
