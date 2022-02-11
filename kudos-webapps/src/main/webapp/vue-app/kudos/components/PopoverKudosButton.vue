<template>    
  <v-tooltip bottom>
    <template v-slot:activator="{ on, attrs }">
      <div
        v-bind="attrs"
        v-on="on">
        <v-btn
          :ripple="false"
          x-small
          icon
          color="primary"
          class="pa-2"
          @click="sendKudos($event)">
          <v-icon>fas fa-award</v-icon>
        </v-btn>
      </div>
    </template>
    <span>
      test test
    </span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    identity: {
      type: Object,
      default: null,
    }
  },
  computed: {
    identityId() {
      return this.identity && this.identity.id;
    }
  },
  methods: {
    sendKudos(event) {
      event.preventDefault();
      event.stopPropagation();
      const type = this.identity && this.identity.prettyName ? 'SPACE_PROFILE' : 'USER_PROFILE';
      const id = this.identity && this.identity.prettyName ? this.identity.id : this.identity.username;
      if (id) {
        document.dispatchEvent(
          new CustomEvent('exo-kudos-open-send-modal', { detail: {
            id: id,
            type: type,
          }}));
      }
    }
  }

};
</script>
