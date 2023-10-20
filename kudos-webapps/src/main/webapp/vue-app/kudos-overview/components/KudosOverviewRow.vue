<template>
  <v-row
    id="kudosOverviewCardsParent"
    class="white border-box-sizing ma-0 align-center">
    <v-col class="kudosOverviewCard">
      <kudos-overview-card
        :clickable="isOwner && receivedKudosCount > 0"
        class="kudosReceivedOverviewPeriod mx-n4"
        @open-drawer="openDrawer('received')">
        <template slot="count">
          {{ receivedKudosCount || '0' }}
        </template>
        <template slot="label">
          {{ $t('exoplatform.kudos.label.received') }}
        </template> 
      </kudos-overview-card>
    </v-col>
    <v-divider
      class="my-9 mx-8 me-md-1 ms-md-5"
      vertical />
    <v-col class="kudosOverviewCard">
      <kudos-overview-card
        :clickable="isOwner && sentKudosCount > 0"
        class="kudosSentOverviewPeriod mx-n4"
        @open-drawer="openDrawer('sent')">
        <template slot="count">
          {{ sentKudosCount || '0' }}
        </template>
        <template slot="label">
          {{ $t('exoplatform.kudos.label.sent') }}
        </template>
      </kudos-overview-card>
    </v-col>
  </v-row>
</template>
<script>
import {getKudosSent, getKudosReceived} from '../../js/Kudos.js'; 
export default {
  props: {
    isOwner: {
      type: Boolean,
      default: false
    }, 
    periodType: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    identityId: eXo.env.portal.profileOwnerIdentityId,
    sentKudosCount: 0,
    receivedKudosCount: 0,
    sentKudos: [],
    receivedKudos: [],
  }),
  watch: {
    periodType() {
      this.refresh();
    }
  },
  created() {
    this.refresh();
  },
  methods: {
    openDrawer(kudosType) {
      if (this.isOwner) {
        const title = kudosType === 'sent' ?
          this.$t('exoplatform.kudos.button.sentKudos'):
          this.$t('exoplatform.kudos.button.receivedKudos');
        this.$refs.kudosOverviewDrawer.open(title, kudosType, this.identityId, this.periodType);
      }
    },
    refresh() {
      const dateInSeconds = parseInt(Date.now() / 1000);
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      getKudosSent(this.identityId, 0, true, this.periodType, dateInSeconds)
        .then(kudosList => {
          this.sentKudosCount = kudosList && kudosList.size || 0;
          this.sentKudos = kudosList && kudosList.kudos || [];
        })
        .then(() => getKudosReceived(this.identityId, 0, true, this.periodType, dateInSeconds))
        .then(kudosList => {
          this.receivedKudosCount = kudosList && kudosList.size || 0;
          this.receivedKudos = kudosList && kudosList.kudos || [];
          return this.$nextTick();
        })
        .finally(() => {
          this.$root.$emit('application-loaded');
          // Decrement 'loading' effect in top of the page
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          document.dispatchEvent(new CustomEvent('kudosCount', {detail: this.sentKudosCount + this.receivedKudosCount}));
        });
    },
  },
};
</script>