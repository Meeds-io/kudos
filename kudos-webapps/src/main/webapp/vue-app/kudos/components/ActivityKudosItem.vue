<template>
  <div v-if="kudos" class="activityKudosItem">
    <exo-user-avatar
      :username="userName"
      :fullname="fullName"
      :avatar-url="avatarUrl"
      :url="profileUrl"
      avatar-class="me-2"
      size="42"
      class="pl-3 pt-2 pb-1"
      bold-title>
      <template slot="subTitle">
        <span v-if="!sameUser" class="caption text-bold">
          {{ inCommonConnections }} {{ $t('UIActivity.label.Reactions_in_Common') }}
        </span>
      </template>
    </exo-user-avatar>
    <v-divider />
  </div>
</template>
<script>

export default {
  props: {
    kudos: {
      type: Object,
      default: null
    }
  },
  data () {
    return {
      userInformations: null,
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
    userName() {
      return this.kudos && this.kudos.senderId;
    },
    fullName() {
      return this.kudos && this.kudos.senderFullName;
    },
    avatarUrl() {
      return this.kudos && this.kudos.senderAvatar;
    },
    profileUrl() {
      return this.kudos && this.userName && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.userName}`;
    },
  },
  created() {
    this.retrieveUserInformations();
  },
  methods: {
    retrieveUserInformations() {
      return this.$userService.getUser(this.kudos.username, 'all,connectionsInCommonCount,relationshipStatus')
        .then(item => this.userInformations = item)
        .catch((e) => {
          console.error('Error while getting user details', e);
        });
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
