<template>
  <div v-if="kudos" class="activityKudosItem">
    <div class="flex-nowrap d-flex flex-shrink-0 align-center">
      <a
        :id="id"
        :href="profileUrl"
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
        <div v-if="fullname || $slots.subTitle" class="pull-left ms-2 d-flex flex-column align-start text-truncate">
          <span
            v-if="fullname">
            <span
              :class="fullnameStyle"
              class="text-truncate subtitle-2 my-auto">{{ fullname }}</span>
            <span v-if="isExternal" class="muted">{{ externalTag }} </span>
            <v-icon size="15" class="pl-2">fa-angle-right</v-icon>
            <span
              v-if="kudosReceiverFullName"
              :class="receiverNameStyle"
              class="text-truncate subtitle-2 my-auto pl-2">{{ kudosReceiverFullName }} </span>
          </span>
          <relative-date-format
            class="text-capitalize-first-letter text-sm-caption text-sub-title text-truncate"
            :value="kudosElapsedTime" />
        </div>
      </a>
      <template v-if="$slots.actions">
        <slot name="actions"></slot>
      </template>
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
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
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
      userInformations: null,
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      isExternal: false,
    };
  },
  computed: {
    inCommonConnections() {
      return this.userInformations && this.userInformations.connectionsInCommonCount || 0;
    },
    sameUser() {
      return this.userInformations && this.userInformations.username === eXo.env.portal.userName;
    },
    notConnected() {
      return this.userInformations && !this.userInformations.relationshipStatus && !this.sameUser;
    },
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${!this.linkStyle && 'text-color' || ''}`;
    },
    receiverNameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.linkStyle && 'text-color' || ''}`;
    },
    username() {
      return this.kudos && this.kudos.senderId;
    },
    fullname() {
      return this.kudos && this.kudos.senderFullName;
    },
    avatarUrl() {
      return this.kudos && this.kudos.senderAvatar;
    },
    profileUrl() {
      return this.kudos && this.userName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.userName}`;
    },
    kudosElapsedTime() {
      return this.$dateUtil.getDateObjectFromString(this.kudosTimeInMilliseconds);
    },
    kudosTimeInMilliseconds() {
      return Number(this.kudos.timeInSeconds) * 1000;
    },
    kudosReceiverFullName() {
      return this.kudos && this.kudos.receiverFullName;
    }
  },
  created() {
    this.retrieveUserInformations();
  },
  methods: {
    retrieveUserInformations() {
      if (this.retrieveExtraInformation && this.fullname) {
        this.$userService.getUser(this.username)
          .then(user => {
            this.isExternal = user.external === 'true';
          });
      } else {
        this.isExternal = this.external;
      }
    },
    connect() {
      this.$userService.connect(this.kudos.username)
        .then(this.retrieveUserInformations())
        .catch((e) => {
          console.error('Error while connecting to user', e);
        });
    },
  },
};
</script>
