<template>
  <user-notification-activity-base
    :notification="notification"
    message-key="Notification.kudos.received"
    icon="fa-award"
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
};
</script>