<template>
  <div class="d-inline-flex pe-1">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-label v-bind="attrs" v-on="on">
          <v-btn
            :id="`KudosActivity${entityId}`"
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
                :class="kudosColorClass"
                class="baseline-vertical-align"
                size="14">
                fa-award
              </v-icon>
              {{ $t('exoplatform.kudos.label.kudos') }}
            </span>
          </v-btn>
        </v-label>
      </template>
      <span>
        {{ buttonDisabled && $t('exoplatform.kudos.info.onlyOtherCanSendYouKudos') || $t('exoplatform.kudos.title.sendAKudos') }}
      </span>
    </v-tooltip>
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          v-show="kudosCount"
          :id="`KudusCountLink${commentId}`"
          :small="!isComment"
          :x-small="isComment"
          class="primary--text font-weight-bold baseline-vertical-align"
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