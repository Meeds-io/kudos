<template>
  <div
    :class="!isComment && 'ms-xl-4 ms-lg-3'"
    class="d-inline-flex">
    <v-tooltip :disabled="isMobile" bottom>
      <template #activator="{ on, attrs }">
        <div
          class="d-flex"
          v-bind="attrs"
          v-on="on">
          <v-btn
            :id="`KudosActivity${entityId}`"
            :class="textColorClass"
            :x-small="isComment"
            :small="!isComment"
            class="pa-0 mt-0"
            text
            link
            @click="openKudosForm">
            <template v-if="isComment">
              {{ $t('exoplatform.kudos.label.kudos') }}
            </template>
            <template v-else>
              <div class="d-flex flex-lg-row flex-column">
                <v-icon
                  :class="kudosColorClass"
                  class="baseline-vertical-align"
                  :size="isMobile && '20' || '16'">
                  fa-award
                </v-icon>
                <span v-if="!isMobile" class="mx-auto mt-1 mt-lg-0 ms-lg-1 text-body">
                  {{ $t('exoplatform.kudos.label.kudos') }}
                </span>
              </div>
            </template>
          </v-btn>
        </div>
      </template>
      <span>
        {{ $t('exoplatform.kudos.title.sendAKudos') }}
      </span>
    </v-tooltip>
    <v-tooltip :disabled="isMobile" bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-show="kudosCount && !isMobile"
          :id="`KudusCountLink${commentId}`"
          :small="!isComment"
          :x-small="isComment"
          class="primary--text font-weight-bold baseline-vertical-align mt-0"
          icon
          v-bind="attrs"
          v-on="on"
          @click="openKudosList">
          ({{ kudosCount }})
        </v-btn>
      </template>
      <span>
        {{ $t('exoplatform.kudos.button.displayKudosList') }}
      </span>
    </v-tooltip>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    linkedKudosList: [],
    limit: 100,
  }),
  computed: {
    entityType() {
      return this.comment && 'COMMENT' || 'ACTIVITY';
    },
    entityId() {
      return (this.comment && this.comment.id && this.comment.id.replace('comment','')) || (this.activity && this.activity.id) || '';
    },
    entityOwner() {
      return (this.comment && this.comment.identity && this.comment.identity.remoteId) || (this.activity && this.activity.identity && this.activity.identity.remoteId) || '';
    },
    parentId() {
      return this.comment && this.comment.activityId || '';
    },
    activityId() {
      return (this.comment && this.comment.activityId) || (this.activity && this.activity.id) || '';
    },
    commentId() {
      return this.comment &&  this.comment.id || '';
    },
    isComment() {
      return !!this.comment;
    },
    spaceURL() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space && this.activity.activityStream.space.prettyName;
    },
    kudosCount() {
      return this.linkedKudosList.length;
    },
    hasSentKudos() {
      return this.linkedKudosList.find(kudos => kudos.senderIdentityId === eXo.env.portal.userIdentityId);
    },
    kudosColorClass() {
      return this.hasSentKudos && 'primary--text' || 'disabled--text';
    },
    textColorClass() {
      return this.hasSentKudos && 'primary--text' || '';
    },
    isCommentOwner() {
      return !this.comment.identity.profile.dataEntity.enabled || this.comment.identity.deleted;
    },
    isActivityOwner() {
      return  !this.activity.identity.profile.dataEntity.enabled || this.activity.identity.deleted;
    },   
    userIdentityId() {
      return  eXo.env.portal.userIdentityId;
    },
    sharedInSpace() {
      return this.activity?.activityStream?.type === 'space';
    },
    isOwner() {
      if (this.comment) {
        const commentOwnerId = this.comment.identity && this.comment.identity.id;
        return commentOwnerId === this.userIdentityId || this.isCommentOwner || (this.sharedInSpace && !this.comment.owner.isMember);
      } else if (this.activity) {
        const activityOwnerId = this.activity.identity && this.activity.identity.id;
        return activityOwnerId === this.userIdentityId || this.isActivityOwner || (this.sharedInSpace && !this.activity.owner.isMember);
      }
      return false;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.resetActivity);
    this.$root.$on('kudos-refresh-comment', this.resetActivityComments);
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.resetActivity);
    this.$root.$off('kudos-refresh-comment', this.resetActivityComments);
  },
  methods: {
    resetActivity(comment) {
      if (!this.comment && comment && comment.activityId === this.activityId) {
        this.$kudosService.resetActivityKudosList(this.activity);
        this.init();
        this.$root.$emit('kudos-refresh-comment', this.activity.id);
      }
    },
    resetActivityComments(activityId) {
      if (activityId && this.comment && activityId === this.activityId) {
        this.init();
      }
    },
    init() {
      if (this.comment) {
        return this.$kudosService.computeCommentKudosList(this.activity, this.comment)
          .then(() => this.linkedKudosList = this.comment.linkedKudosList || []);
      } else {
        return this.$kudosService.computeActivityKudosList(this.activity)
          .then(() => this.linkedKudosList = this.activity.linkedKudosList || []);
      }
    },
    openKudosForm(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.entityId,
        parentId: this.parentId,
        type: this.entityType,
        owner: !this.isOwner && this.entityOwner || null,
        spaceURL: this.spaceURL
      }}));
    },
    openKudosList(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('open-reaction-drawer-selected-tab', {detail: {
        activityId: this.isComment ? this.commentId : this.activityId,
        tab: 'kudos',
        activityType: this.entityType
      }}));
    },
  },
};
</script>
