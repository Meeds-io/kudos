<template>
  <v-btn
    :id="`KudosActivity${entityId}`"
    :title="$t('exoplatform.kudos.title.sendAKudos')"
    :class="textColorClass"
    class="pa-0 mx-2"
    text
    link
    small
    @click="openKudosForm">
    <v-icon
      :class="iconColorClass"
      class="me-1 pb-1"
      size="12">
      fa-award
    </v-icon>
    <span>{{ $t('exoplatform.kudos.label.kudos') }}</span>
  </v-btn>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    hasSentKudos: false,
  }),
  computed: {
    entityType() {
      return this.activity && this.activity.activity && 'COMMENT' || 'ACTIVITY';
    },
    entityId() {
      return this.activity && (this.activity.activity && this.activity.id.replace('comment','') || this.activity.id) || '';
    },
    parentId() {
      return this.activity && this.activity.activity && this.activity.activity.id || '';
    },
    iconColorClass() {
      return this.hasSentKudos && 'primary--text' || 'disabled--text';
    },
    textColorClass() {
      return this.hasSentKudos && 'primary--text' || '';
    },
  },
  created() {
    if (this.activity) {
      this.refresh();
    }
    document.addEventListener('exo-kudos-sent', event => {
      const kudos = event && event.detail;
      if (kudos && kudos.entityType === this.entityType && kudos.entityId === this.entityId) {
        this.refresh();
      }
    });
  },
  methods: {
    refresh() {
      return this.$kudosService.countUserKudosSentByEntity(this.entityType, this.entityId)
        .then(hasSentKudos => this.hasSentKudos = hasSentKudos && Number(hasSentKudos));
    },
    openKudosForm(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {
        detail: {
          id: this.entityId,
          parentId: this.parentId,
          type: this.entityType,
        }}));
    },
  },
};
</script>