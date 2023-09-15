<template>
  <v-btn
    id="NewUserKudosButton"
    class="btn primary px-2"
    outlined
    small
    @click.stop.prevent="openKudosForm">
    {{ helloIcon }} {{ $t('Notification.plugin.NewUserKudosButtonTitle') }}
  </v-btn>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    helloIcon: window.decodeURIComponent('%F0%9F%91%8B'),
  }),
  methods: {
    openKudosForm() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.notification?.from?.username,
        type: 'USER_PROFILE',
      }}));
    },
  },
};
</script>