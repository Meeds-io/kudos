<template>
  <v-app
    :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'">
    <widget-wrapper
      id="kudosOverviewHeader"
      :title="$t('exoplatform.kudos.button.rewardedKudos')"
      extra-class="application-body">
      <template #action>
        <div class="position-relative">
          <select
            v-model="periodType"
            :class="$vuetify.rtl && 'l-0' || 'r-0'"
            class="kudosOverviewPeriodSelect absolute-vertical-center header-height width-auto my-auto py-0 ignore-vuetify-classes"
            size="1">
            <option
              v-for="period in periods"
              :key="period.value"
              :value="period.value">
              {{ period.text }}
            </option>
          </select>
        </div>
      </template>
      <kudos-overview-row
        :period-type="periodType"
        class="my-auto"
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