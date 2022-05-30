<template>
  <div
    :class="!isComment && 'ms-lg-4'"
    class="d-inline-flex">
        <div class="d-flex">
          <v-btn
            :title="buttonDisabled && $t('exoplatform.kudos.info.onlyOtherCanSendYouKudos') || $t('exoplatform.kudos.title.sendAKudos')"
            :id="`KudosActivity${entityId}`"
            :disabled="buttonDisabled"
            :class="textColorClass"
            :small="!isComment"
            :x-small="isComment"
            class="pa-0 mt-0"
            text
            link
            @click="openKudosForm"
            v-bind="attrs"
            v-on="on">
            <template v-if="isComment">
              {{ $t('exoplatform.kudos.label.kudos') }}
            </template>
            <template v-else>
              <div class="d-flex flex-lg-row flex-column">
                <v-icon
                  :class="kudosColorClass"
                  class="baseline-vertical-align"
                  size="14">
                  fa-award
                </v-icon>
                <span class="mx-auto mt-1 mt-lg-0 ms-lg-2">
                  {{ $t('exoplatform.kudos.label.kudos') }}
                </span>
              </div>
            </template>
          </v-btn>
        </div>
        <v-btn
          :title="$t('exoplatform.kudos.button.displayKudosList')"
          v-show="kudosCount"
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
    buttonDisabled() {
      if (this.comment) {
        const commentOwnerId = this.comment.identity && this.comment.identity.id;
        return commentOwnerId === eXo.env.portal.userIdentityId;
      } else if (this.activity) {
        const activityOwnerId = this.activity.identity && this.activity.identity.id;
        return activityOwnerId === eXo.env.portal.userIdentityId;
      }
      return false;
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
        owner: this.entityOwner,
      }}));
    },
    openKudosList(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent(`open-reaction-drawer-selected-tab-${this.activityId}`, {detail: {
        activityId: this.isComment ? this.commentId : this.activityId,
        tab: 'kudos',
        activityType: this.entityType
      }}));
    },
  },
};
</script>