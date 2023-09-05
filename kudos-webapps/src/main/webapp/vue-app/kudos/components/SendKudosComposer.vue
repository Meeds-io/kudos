<template>
  <v-card
    class="mx-4 mb-3 px-6 py-3"
    outlined 
    flat 
    hover>
    <div
      class="d-flex flex-row align-center" 
      @click="openSendKudosDrawer">
      <v-icon
        color="primary"
        size="50">
        fa-award
      </v-icon>
      <v-span class="caption font-weight-bold ms-5">
        {{ $t('exoplatform.kudos.title.sendAKudos') }}
      </v-span>
    </div>
  </v-card>
</template>
<script>
export default {
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('activity-composer-closed'));
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: eXo.env.portal.userIdentityId,
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
        spaceURL: eXo.env.portal.spaceUrl,
        readOnlySpace: true
      }}));
    },
  },
};
</script> 