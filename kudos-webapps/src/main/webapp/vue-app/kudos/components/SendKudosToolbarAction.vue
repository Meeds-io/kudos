<template>
  <v-btn
    id="kudosBtnToolbar"
    :icon="!spaceId"
    :text="spaceId"
    :class="kudosButtonStyle"
    @click="openSendKudosDrawer">
    <v-icon
      :size="iconSize"
      color="primary">
      fa-award
    </v-icon>
    <v-span v-if="spaceId" class="body-2 font-weight-bold ms-5  mt-1 dark-grey-color">
      {{ $t('kudos.title') }}
    </v-span>
  </v-btn>
</template>
<script>
export default {
  data () {
    return {
      spaceId: eXo.env.portal.spaceId,
    };
  },
  computed: {
    iconSize() {
      return this.spaceId ? '27' : '21';
    },
    kudosButtonStyle() {
      return this.spaceId && 'd-flex flex-row align-center py-2 px-3';
    }
  },
  methods: {
    openSendKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: eXo.env.portal.userIdentityId,
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
        spaceURL: eXo.env.portal.spaceUrl,
        readOnlySpace: eXo.env.portal.spaceUrl ? true : false
      }}));
    },
  },
};
</script> 