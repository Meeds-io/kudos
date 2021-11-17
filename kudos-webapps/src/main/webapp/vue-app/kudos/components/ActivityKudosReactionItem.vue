<template>
  <div v-if="kudos" class="activityKudosItem">
    <div class="flex-nowrap d-flex flex-shrink-0 align-center">
      <a
        :id="senderId"
        :href="senderProfileUrl"
        class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid">
        <v-avatar
          :size="size"
          :class="avatarClass"
          class="ma-0 pull-left">
          <img
            :src="avatarUrl"
            class="object-fit-cover ma-auto"
            loading="lazy">
        </v-avatar>
        <div class="pull-left ms-2 d-flex flex-column align-start text-truncate">
          <span
            v-if="KudosSenderFullName">
            <span
              class="font-weight-bold text-color text-truncate subtitle-2 my-auto">{{ KudosSenderFullName }}</span>
            <span v-if="isExternal" class="muted">{{ externalTag }} </span>
            <v-icon size="15" class="pl-2">fa-angle-right</v-icon>
            <a
              :id="receiverId"
              :href="receiverProfileUrl">
              <span
                v-if="kudosReceiverFullName"
                class="font-weight-bold text-truncate subtitle-2 my-auto pl-1">{{ kudosReceiverFullName }} </span>
            </a>
          </span>
          <relative-date-format
            class="text-sm-caption text-sub-title"
            :value="kudosElapsedTime" />
        </div>
      </a>
    </div>
    <v-divider />
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
    retrieveExtraInformation: {
      type: Boolean,
      default: true,
    },
    avatarClass: {
      type: String,
      default: () => '',
    },
    tiptip: {
      type: Boolean,
      default: () => true,
    },
    tiptipPosition: {
      type: String,
      default: () => null,
    },
    title: {
      type: String,
      default: function() {
        return `${this.title}`;
      }
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
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
      isExternal: false,

    };
  },
  computed: {
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
    KudosSenderFullName() {
      return this.kudos && this.kudos.senderFullName;
    },
    avatarUrl() {
      return this.kudos && this.kudos.senderAvatar;
    },
    senderProfileUrl() {
      return this.kudos && this.userName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.userName}`;
    },
    receiverProfileUrl() {
      return this.kudos && this.userName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.userName}`;
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
    kudosReceiverFullName() {
      return this.kudos && this.kudos.receiverFullName;
    }
  },
  created() {
    this.retrieveUserInformations();
  },
  mounted() {
    if (this.KudosSenderUsername && this.kudosReceiverUsername && this.tiptip) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      const users = [
        {
          id: this.senderId,
          username: this.username
        },
        {
          id: this.receiverId ,
          username: this.kudosReceiverUsername
        }
      ];
      users.forEach(user => {
        this.$nextTick(() => {
          $(`#${user.id}`).userPopup({
            restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
            userId: user.username,
            keepAlive: true,
          });
        });
      });
    },
    retrieveUserInformations() {
      if (this.retrieveExtraInformation && this.KudosSenderFullName) {
        this.$userService.getUser(this.KudosSenderUsername)
          .then(user => {
            this.isExternal = user.external === 'true';
          });
      } else {
        this.isExternal = this.external;
      }
    },
  },
};
</script>
