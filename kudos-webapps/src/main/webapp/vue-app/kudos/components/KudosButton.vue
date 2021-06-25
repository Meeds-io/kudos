<template>
  <div class="d-inline-flex pe-1 ms-1">
    <v-btn
      :id="`KudosActivity${entityId}`"
      :title="$t('exoplatform.kudos.title.sendAKudos')"
      :disabled="buttonDisabled"
      :class="textColorClass"
      :small="!isComment"
      :x-small="isComment"
      class="px-0 width-auto"
      text
      link
      @click="openKudosForm">
      <span>
        <v-icon
          v-if="!isComment"
          class="baseline-vertical-align primary--text"
          size="14">
          fa-award
        </v-icon>
        {{ $t('exoplatform.kudos.label.kudos') }}
      </span>
    </v-btn>
    <v-btn
      v-if="kudosCount"
      :id="`KudusCountLink${commentId}`"
      :title="$t('exoplatform.kudos.button.displayKudosList')"
      :small="!isComment"
      :x-small="isComment"
      class="primary--text font-weight-bold baseline-vertical-align"
      icon
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
    parentId() {
      return this.comment && this.comment.activityId || '';
    },
    activityId() {
      return (this.comment && this.comment.activityId) || (this.activity && this.activity.id) || '';
    },
    parentCommentId() {
      return this.comment && (this.comment.parentCommentId || this.comment.id) || '';
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
    document.addEventListener('activity-comment-created', this.resetActivity);
    this.init();
  },
  beforeDestroy() {
    document.removeEventListener('activity-comment-created', this.resetActivity);
  },
  methods: {
    resetActivity(event) {
      if (!this.comment) {
        const activityId = event && event.detail && event.detail.activityId;
        if (activityId === this.activity.id) {
          this.init();
        }
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
      }}));
    },
    openKudosList(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('exo-kudos-open-kudos-list', {detail: {
        id: this.entityId,
        parentId: this.parentId,
        type: this.entityType,
      }}));
    },
  },
};
</script>