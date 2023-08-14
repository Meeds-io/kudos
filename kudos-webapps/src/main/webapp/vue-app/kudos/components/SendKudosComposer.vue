<template>
  <div>
    <v-card 
      class="d-flex flex-row" 
      @click="openSendKudosDrawer" 
      outlined 
      flat 
      hover>
      <v-card-actions class="ms-3 me-2">
        <v-icon
          color="primary"
          size="50">
          fa-award
        </v-icon>
      </v-card-actions>
      <v-card-title class="subtitle-1 font-weight-bold">
        {{ $t('exoplatform.kudos.title.sendAKudos') }}
      </v-card-title>
    </v-card>
  </div>
</template>
<script>
export default {
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: eXo.env.portal.spaceId,
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
        spaceURL: eXo.env.portal.spaceURL,
        readOnlySpace: true
      }}));
    },
  },
};
</script> 