<template>
  <a
    v-if="id"
    :id="componentId"
    :title="id"
    :href="url"
    rel="nofollow"
    target="_blank">
    <span v-text="name"></span>
  </a>
</template>

<script>
export default {
  props: {
    technicalId: {
      type: String,
      default: function() {
        return null;
      }
    },
    name: {
      type: String,
      default: function() {
        return null;
      }
    },
    id: {
      type: String,
      default: function() {
        return null;
      }
    },
    type: {
      type: String,
      default: function() {
        return null;
      }
    }
  },
  data () {
    return {
      componentId: `ReceiverLink${parseInt(Math.random() * 10000).toString().toString()}`,
      labels: {
        CancelRequest: this.$t('exoplatform.kudos.label.profile.CancelRequest'),
        Confirm: this.$t('exoplatform.kudos.label.profile.Confirm'),
        Connect: this.$t('exoplatform.kudos.label.profile.Connect'),
        Ignore: this.$t('exoplatform.kudos.label.profile.Ignore'),
        RemoveConnection: this.$t('exoplatform.kudos.label.profile.RemoveConnection'),
        StatusTitle: this.$t('exoplatform.kudos.label.profile.StatusTitle'),
        join: this.$t('exoplatform.kudos.label.profile.join'),
        leave: this.$t('exoplatform.kudos.label.profile.leave'),
        members: this.$t('exoplatform.kudos.label.profile.members'),
      }
    };
  },
  computed: {
    url() {
      if (this.type === 'user' || this.type === 'organization') {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.id}`;
      } else {
        return `${eXo.env.portal.context}/g/:spaces:${this.id}/`;
      }
    }
  },
  watch: {
    id() {
      if (this.id) {
        // TODO disable tiptip because of high CPU usage using its code
        this.initTiptip();
      }
    }
  },
  created() {
    if (this.id) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      if (this.type === 'space') {
        this.$nextTick(() => {
          $(`#${this.componentId}`).spacePopup({
            userName: eXo.env.portal.userName,
            spaceID: this.technicalId,
            restURL: '/portal/rest/v1/social/spaces/{0}',
            membersRestURL: '/portal/rest/v1/social/spaces/{0}/users?returnSize=true',
            managerRestUrl: '/portal/rest/v1/social/spaces/{0}/users?role=manager&returnSize=true',
            membershipRestUrl: '/portal/rest/v1/social/spacesMemberships?space={0}&returnSize=true',
            defaultAvatarUrl: `/portal/rest/v1/social/spaces/${this.id}/avatar`,
            deleteMembershipRestUrl: '/portal/rest/v1/social/spacesMemberships/{0}:{1}:{2}',
            labels: this.labels,
            content: false,
            keepAlive: true,
            defaultPosition: 'left',
            maxWidth: '240px'
          });
        });
      } else {
        this.$nextTick(() => {
          $(`#${this.componentId}`).userPopup({
            restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
            userId: this.id,
            labels: this.labels,
            content: false,
            keepAlive: true,
            defaultPosition: 'left',
            maxWidth: '240px'
          });
        });
      }
    }
  }
};
</script>