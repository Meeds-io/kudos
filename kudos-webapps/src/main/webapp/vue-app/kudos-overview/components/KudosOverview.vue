<template>
  <v-app
    :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'"
    class="white">
    <widget-wrapper
      id="kudosOverviewHeader"
      :title="$t('exoplatform.kudos.button.rewardedKudos')">
      <template #action>
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
      </template>
      <kudos-overview-row
        :period-type="periodType"
        @loading="loading = $event"
        @has-kudos="hasKudos = $event" />
    </widget-wrapper>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    periodType: 'WEEK',
    loading: true,
    hasKudos: false,
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
};
</script>