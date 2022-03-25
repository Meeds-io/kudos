<template>    
  <v-tooltip bottom>
    <template v-slot:activator="{ on, attrs }">
      <div
        v-bind="attrs"
        v-on="on">
        <v-btn
          :ripple="false"
          icon
          color="primary"
          @click="sendKudos($event)">
          <v-icon size="18">fas fa-award</v-icon>
        </v-btn>
      </div>
    </template>
    <span>
      {{ $t('exoplatform.kudos.title.sendAKudos') }}
    </span>
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
    entityOwner: {
      type: String,
      default: ''
    }
  },
  methods: {
    sendKudos(event) {
      event.preventDefault();
      event.stopPropagation();
      document.dispatchEvent(
        new CustomEvent('exo-kudos-open-send-modal', { detail: {
          owner: this.entityOwner,
          id: this.identityId,
          type: this.identityType,
        }}));
    }
  }

};
</script>
