<template>
  <v-tooltip v-if="!spaceId" bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        id="kudosBtnToolbar"
        height="36px"
        width="36px"
        icon
        v-bind="attrs"
        v-on="on"
        @click="openSendKudosDrawer">
        <v-icon
          size="20px"
          color="primary">
          fa-award
        </v-icon>
      </v-btn>
    </template>
    <span>
      {{ $t('exoplatform.kudos.tooltip') }}
    </span>
  </v-tooltip>
  <v-btn
    v-else
    id="kudosBtnToolbar"
    class="d-flex flex-row align-center py-2 px-3"
    text
    @click="openSendKudosDrawer">
    <v-icon
      size="27px"
      color="primary">
      fa-award
    </v-icon>
    <v-span class="body-2 font-weight-bold ms-5  mt-1 dark-grey-color">
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