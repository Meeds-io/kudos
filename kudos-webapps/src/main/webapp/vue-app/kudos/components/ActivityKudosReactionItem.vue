<template>
  <div v-if="kudos" class="activityKudosItem">
    <div class="flex-nowrap d-flex flex-shrink-0 align-center">
      <exo-user-avatar
        :profile-id="KudosSenderUsername"
        :size="42"
        avatar
        popover />
      <div class="pull-left ms-2 d-flex flex-column align-start text-truncate">
        <div
          v-if="KudosSenderFullName"
          class="d-flex">
          <exo-user-avatar
            :profile-id="KudosSenderUsername"
            fullname
            bold-title
            popover />
          <v-icon size="15" class="px-2">fa-angle-right</v-icon>
          <exo-user-avatar
            :profile-id="kudosReceiverUsername"
            fullname
            bold-title
            popover />
        </div>
        <relative-date-format
          class="text-sm-caption text-sub-title text-light-color"
          :value="kudosElapsedTime" />
      </div>
    </div>
  </div>
</template>
<script>
const randomMax = 10000;

export default {
  props: {
    kudos: {
      type: Object,
      default: null
    },
  },
  data () {
    return {
      senderId: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      receiverId: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    KudosSenderFullName() {
      return this.kudos && this.kudos.senderFullName;
    },
    kudosElapsedTime() {
      return this.$dateUtil.getDateObjectFromString(this.kudosTimeInMilliseconds);
    },
    kudosTimeInMilliseconds() {
      return Number(this.kudos.timeInSeconds) * 1000;
    },
    KudosSenderUsername() {
      return this.kudos && this.kudos.senderId;
    },
    kudosReceiverUsername () {
      return this.kudos && this.kudos.receiverId;
    },
  },
};
</script>
