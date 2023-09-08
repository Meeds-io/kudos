<template>
  <v-btn
    :ripple="false"
    class="d-flex flex-row align-center py-2"
    text
    @click="openSendKudosDrawer">
    <v-icon
      color="primary"
      size="27">
      fa-award
    </v-icon>
    <v-span class="body-2 font-weight-bold ms-5  mt-1 dark-grey-color">
      {{ $t('kudos.title') }}
    </v-span>
  </v-btn>
</template>
<script>
export default {
  methods: {
    openSendKudosDrawer() {
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