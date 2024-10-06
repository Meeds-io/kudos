<template>    
  <v-tooltip bottom>
    <template #activator="{ on, attrs }">
      <div
        v-bind="attrs"
        v-on="on">
        <div v-if="enabled">  
          <v-btn
            :ripple="false"
            icon
            color="primary"
            @click="sendKudos($event)">
            <v-icon size="18">fa-award</v-icon>
          </v-btn>
        </div>
      </div>
    </template>
    <span>{{ $t('exoplatform.kudos.title.sendAKudos') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    identityType: {
      type: String,
      default: '',
    },
    identityId: {
      type: String,
      default: ''
    },
    identityEnabled: {
      type: String,
      default: '',
    },  
    identityDeleted: {
      type: String,
      default: ''
    },
    spacePrettyName: {
      type: String,
      default: '',
    },
    canRedactOnSpace: {
      type: String,
      default: '',
    },
  },
  computed: {
    enabled() {
      return !this.identityDeleted && this.identityEnabled !== false && this.canRedactOnSpace !== false;
    },
    isSpace() {
      return this.identityType === 'space' || this.identityType === 'SPACE_TIPTIP' || this.identityType === 'SPACE_PROFILE';
    },
  },
  methods: {
    sendKudos(event) {
      event.preventDefault();
      event.stopPropagation();
      document.dispatchEvent(
        new CustomEvent('exo-kudos-open-send-modal', { detail: {
          id: this.identityId,
          type: this.isSpace ? 'SPACE_TIPTIP' : this.identityType,
          spacePrettyName: this.isSpace ? this.spacePrettyName || eXo.env.portal.prettyName : null,
          spaceId: this.isSpace ? this.identityId : null,
          readOnlySpace: this.isSpace,
        }}));
    }
  },
};
</script>
