<template>
  <v-list-item
    :href="activityUrl"
    two-line
    class="ma-4">
    <v-list-item-avatar :tile="isSpace" :class="isSpace && 'spaceAvatar'">
      <v-img
        :src="avatar" />
    </v-list-item-avatar>
    <v-list-item-content class="py-0">
      <v-list-item-title :id="id" :href="url">
        {{ fullName }}
      </v-list-item-title>
      <v-list-item-subtitle>
        {{ dateTime }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-icon class="my-auto">
      <v-icon>mdi-chevron-right</v-icon>
    </v-list-item-icon>
  </v-list-item>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    kudosItem: {
      type: Object,
      default: () => ({}),
    },
    type: {
      type: String,
      default: 'sent',
    },
  },
  data: () => ({
    id: `avatar${parseInt(Math.random() * randomMax)
      .toString()
      .toString()}`,
    lang: eXo.env.portal.language,
    dateFormat: {
      dateStyle: 'long',
      timeStyle: 'short',
    },
  }),
  computed: {
    labels() {
      return {
        join: this.$t('exoplatform.kudos.label.profile.join'),
        leave: this.$t('exoplatform.kudos.label.profile.leave'),
        members: this.$t('exoplatform.kudos.label.profile.members'),
      };
    },
    isSender() {
      return this.kudosItem && Number(this.kudosItem.senderIdentityId) === Number(eXo.env.portal.profileOwnerIdentityId);
    },
    isSpace() {
      return this.isSender && this.kudosItem.receiverType === 'space';
    },
    avatar() {
      return this.isSender && this.kudosItem.receiverAvatar || this.kudosItem.senderAvatar;
    },
    fullName() {
      return this.isSender && this.kudosItem.receiverFullName || this.kudosItem.senderFullName;
    },
    remoteId() {
      return this.isSender && this.kudosItem.receiverId || this.kudosItem.senderId;
    },
    identityId() {
      return this.isSender && this.kudosItem.receiverIdentityId || this.kudosItem.senderIdentityId;
    },
    url() {
      return this.isSender && this.kudosItem.receiverURL || this.kudosItem.senderURL;
    },
    dateTime() {
      if (!this.kudosItem || !this.kudosItem.timeInSeconds) {
        return '';
      }
      const dateTime = new Date(this.kudosItem.timeInSeconds * 1000);
      return new window.Intl.DateTimeFormat(this.lang, this.dateFormat).format(dateTime);
    },
    activityUrl() {
      if (!this.kudosItem) {
        return null;
      }
      const activityId = this.kudosItem.entityType === 'COMMENT' && this.kudosItem.parentEntityId 
        || this.kudosItem.entityType === 'ACTIVITY' && this.kudosItem.entityId
        || this.kudosItem.activityId;
      let commentId = null;
      if (this.kudosItem.entityType === 'COMMENT' || this.kudosItem.entityType === 'ACTIVITY') {
        commentId = this.kudosItem.activityId;
      }
      return this.kudosItem && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${activityId}${commentId && '#comment-comment' || ''}${commentId || ''}`;
    },
  },
  mounted() {
    if (this.identityId) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      if (this.isSpace) {
        this.$nextTick(() => {
          $(`#${this.id}`).spacePopup({
            userName: eXo.env.portal.userName,
            spaceID: this.identityId,
            restURL: '/portal/rest/v1/social/spaces/{0}',
            membersRestURL: '/portal/rest/v1/social/spaces/{0}/users?returnSize=true',
            managerRestUrl: '/portal/rest/v1/social/spaces/{0}/users?role=manager&returnSize=true',
            membershipRestUrl: '/portal/rest/v1/social/spacesMemberships?space={0}&returnSize=true',
            defaultAvatarUrl: this.avatar,
            deleteMembershipRestUrl: '/portal/rest/v1/social/spacesMemberships/{0}:{1}:{2}',
            labels: this.labels,
            content: false,
            keepAlive: true,
            defaultPosition: 'left_bottom',
            maxWidth: '420px',
          });
        });
      } else {
        this.$nextTick(() => {
          $(`#${this.id}`).userPopup({
            restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
            userId: this.remoteId,
            labels: this.labels,
            keepAlive: true,
          });
        });
      }
    },
  }
};
</script>
