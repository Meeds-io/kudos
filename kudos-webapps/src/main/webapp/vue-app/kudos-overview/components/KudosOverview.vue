<template>
  <v-app
    :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'"
    class="white">
    <v-toolbar
      v-if="!isOverviewDisplay"
      id="kudosOverviewHeader"
      color="white"
      height="64"
      flat
      class="border-box-sizing">
      <div class="d-flex flex py-3">
        <div class="text-header-title text-sub-title text-no-wrap d-flex align-center">
          {{ $t('exoplatform.kudos.button.rewardedKudos') }}
        </div>
        <v-spacer />
        <select
          v-model="periodType"
          class="kudosOverviewPeriodSelect fill-height col-auto my-auto py-0 subtitle-1 ignore-vuetify-classes">
          <option
            v-for="period in periods"
            :key="period.value"
            :value="period.value">
            {{ period.text }}
          </option>
        </select>
      </div>
    </v-toolbar>
    <v-row
      id="kudosOverviewCardsParent"
      class="white border-box-sizing px-4 py-0 ma-0 align-center">
      <v-col class="kudosOverviewCard">
        <kudos-overview-card
          :clickable="owner && receivedKudosCount > 0"
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
          :clickable="owner && sentKudosCount > 0"
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

    <kudos-overview-drawer
      v-if="owner"
      ref="kudosOverviewDrawer" />
  </v-app>
</template>

<script>
import {getKudosSent, getKudosReceived} from '../../js/Kudos.js'; 

export default {
  props: {
    isOverviewDisplay: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    identityId: eXo.env.portal.profileOwnerIdentityId,
    periodType: 'WEEK',
    sentKudosCount: 0,
    receivedKudosCount: 0,
    sentKudos: [],
    receivedKudos: [],
  }),
  computed: {
    periods() {
      return [{
        text: this.$t('exoplatform.kudos.label.week'),
        value: 'WEEK',
      } , {
        text: this.$t('exoplatform.kudos.label.month'),
        value: 'MONTH',
      } , {
        text: this.$t('exoplatform.kudos.label.quarter'),
        value: 'QUARTER',
      } , {
        text: this.$t('exoplatform.kudos.label.semester'),
        value: 'SEMESTER',
      } , {
        text: this.$t('exoplatform.kudos.label.year'),
        value: 'YEAR',
      }];
    },
  },
  watch: {
    periodType() {
      this.refresh();
    },
  },
  created() {
    this.refresh();
  },
  methods: {
    openDrawer(kudosType) {
      if (this.owner) {
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