<template>
  <v-card 
    class="d-flex flex-row" 
    outlined 
    flat 
    hover
    @click="openSendKudosDrawer" >
    <v-card-actions class="ms-4 py-3">
      <v-icon
        color="primary"
        size="50">
        fa-award
      </v-icon>
    </v-card-actions>
    <v-card-title class="caption font-weight-bold">
      {{ $t('exoplatform.kudos.title.sendAKudos') }}
    </v-card-title>
  </v-card>
</template>
<script>
export default {
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('activity-composer-closed'));
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: eXo.env.portal.spaceId,
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