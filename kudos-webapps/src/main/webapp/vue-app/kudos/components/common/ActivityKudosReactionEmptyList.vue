<template>
  <div class="no-kudos-block flex flex-column align-center text-sub-title pt-12">
    <v-icon
      class="text-sub-title"
      size="64">
      fa-award
    </v-icon>
    <span class="no-kudos-yet-label pt-5">
      {{ $t('exoplatform.kudos.info.noKudosYet') }}
      <a
        class="text-decoration-underline pl-1"
        v-if="!sameUser"
        @click="openKudosDrawer"> {{ $t('exoplatform.kudos.info.sendAKudos') }} </a>
    </span>
    <span v-if="!sameUser" class="kudos-message-purpose-label"> {{ $t('exoplatform.kudos.label.kudosMessagePurpose') }}</span>
  </div>
</template>
<script>
export default {
  props: {
    activityPosterId: {
      type: String ,
      default: () => ''
    },
    activityId: {
      type: String ,
      default: () => ''
    },
    parentId: {
      type: String,
      default: () => ''
    },
    activityType: {
      type: String ,
      default: () => ''
    }
  },
  computed: {
    sameUser() {
      return this.activityPosterId && this.activityPosterId === eXo.env.portal.userName;
    },
    isComment() {
      return this.activityType === 'COMMENT';
    }
  },
  methods: {
    openKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.isComment ? this.activityId.replace('comment','') : this.activityId,
        parentId: this.parentId,
        type: this.activityType,
      }}));
    }
  }
};
</script>
