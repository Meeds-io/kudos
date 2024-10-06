<template>
  <user-notification-activity-base
    :notification="notification"
    :message-key="messageKey"
    icon="fa-award pt-2px"
    reply-icon="far fa-handshake"
    reply-key="kudos.notification.button"
    reply />
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    messageKey() {
      return this.notification?.parameters?.RECEIVER_TYPE === 'space' ? 'Notification.kudos.spaceReceived.web' : 'Notification.kudos.received';
    },
  }
};
</script>