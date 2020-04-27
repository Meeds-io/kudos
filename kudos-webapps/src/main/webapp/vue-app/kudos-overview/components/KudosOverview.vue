<template>
  <v-app
    :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'"
    class="white">
    <v-toolbar
      color="white"
      height="48"
      flat
      class="border-box-sizing py-3">
      <div
        :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-thick skeleton-border-radius'"
        class="text-header-title text-sub-title text-no-wrap">
        {{ $t('exoplatform.kudos.button.rewardedKudos') }}
      </div>
      <v-spacer />
      <select
        v-model="periodType"
        :disabled="skeleton"
        :class="skeleton && 'skeleton-background skeleton-text'"
        class="kudosOverviewPeriodSelect fill-height col-auto mr-2 my-auto px-3 py-0 subtitle-1 ignore-vuetify-classes">
        <option
          v-for="period in periods"
          :key="period.value"
          :value="period.value">
          {{ period.text }}
        </option>
      </select>
    </v-toolbar>
    <v-row id="kudosOverviewCardsParent" class="white border-box-sizing px-4 py-0 ma-0">
      <v-col class="kudosOverviewCard">
        <kudos-overview-card
          :clickable="owner && receivedKudosCount > 0"
          :skeleton="skeleton"
          :class="skeleton && 'skeleton-background skeleton-text skeleton-border-radius skeleton-text-height-block mx-10 my-4'"
          class="kudosReceivedOverviewPeriod"
          @open-drawer="openDrawer('received')">
          <template slot="count">
            {{ receivedKudosCount || '0' }}
          </template>
          <template slot="label">
            {{ $t('exoplatform.kudos.button.receivedKudos') }}
          </template>
        </kudos-overview-card>
      </v-col>
      <v-divider class="my-4" vertical />
      <v-col class="kudosOverviewCard">
        <kudos-overview-card
          :clickable="owner && sentKudosCount > 0"
          :skeleton="skeleton"
          :class="skeleton && 'skeleton-background skeleton-text skeleton-border-radius skeleton-text-height-block mx-10 my-4'"
          class="kudosSentOverviewPeriod"
          @open-drawer="openDrawer('sent')">
          <template slot="count">
            {{ sentKudosCount || '0' }}
          </template>
          <template slot="label">
            {{ $t('exoplatform.kudos.button.sentKudos') }}
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
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    identityId: eXo.env.portal.profileOwnerIdentityId,
    periodType: 'WEEK',
    sentKudosCount: 0,
    receivedKudosCount: 0,
    sentKudos: [],
    receivedKudos: [],
    skeleton: true,
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
  mounted() {
    // Decrement 'loading' effect after having incremented it in main.js
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
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
        })
        .finally(() => {
          // Decrement 'loading' effect in top of the page
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          this.skeleton = false;
        });
    },
  },
};
</script>