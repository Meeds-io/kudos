<template>
  <v-layout wrap class="mx-4">
    <v-flex
      id="selectedDateMenu"
      class="text-center"
      xs2>
      <v-combobox
        v-model="kudosPeriodType"
        :items="periods"
        :return-object="false"
        :label="$t('exoplatform.kudos.label.periodType')"
        hide-no-data
        hide-selected
        small-chips
        class="kudosPeriodTypeInput">
        <!-- Without slot-scope, the template isn't displayed -->
        <!-- eslint-disable-next-line vue/no-unused-vars -->
        <template slot="selection" slot-scope="data">
          {{ selectedPeriodType }}
        </template>
      </v-combobox>
    </v-flex>
    <v-flex xs10>
      <v-text-field
        v-model="periodDatesDisplay"
        :label="$t('exoplatform.kudos.label.selectPeriodDate')"
        prepend-icon="event"
        @click="selectedDateMenu = true" />
      <v-menu
        ref="selectedDateMenu"
        v-model="selectedDateMenu"
        :nudge-right="$vuetify.rtl"
        attach="#selectedDateMenu"
        transition="scale-transition"
        offset-y
        offset-x
        eager
        class="kudosDateSelector">
        <v-date-picker
          v-model="selectedDate"
          :first-day-of-week="1"
          :type="!periodType || periodType === 'WEEK' ? 'date' : 'month'"
          :locale="lang"
          class="border-box-sizing"
          @input="selectedDateMenu = false" />
      </v-menu>
    </v-flex>
    <v-flex xs12>
      <v-data-table
        :headers="kudosIdentitiesHeaders"
        :items="kudosIdentitiesList"
        :items-per-page="1000"
        :loading="loading"
        :sortable="true"
        class="elevation-1 me-3 mb-2"
        hide-default-footer>
        <template slot="item" slot-scope="props">
          <tr>
            <td>
              <v-avatar size="36px">
                <img
                  :src="props.item.avatar"
                  onerror="this.src = '/eXoSkin/skin/images/system/SpaceAvtDefault.png'">
              </v-avatar>
            </td>
            <td class="text-start">
              <a
                :href="props.item.url"
                rel="nofollow"
                target="_blank">
                {{ props.item.name }}
              </a>
            </td>
            <td v-text="props.item.received">
            </td>
            <td v-text="props.item.sent">
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-flex>
  </v-layout>
</template>

<script>
export default {
  props: {
    defaultKudosPeriodType: {
      type: String,
      default: function() {
        return null;
      }
    }
  },
  data() {
    return {
      loading: false,
      error: null,
      selectedDate: null,
      selectedDateMenu: false,
      kudosPeriodType: null,
      kudosIdentitiesList: [],
      selectedStartDateInSeconds: null,
      selectedEndDateInSeconds: null,
      lang: 'en',
    };
  },
  computed: {
    kudosIdentitiesHeaders(){
      return [
        {
          text: '',
          sortable: false,
          value: 'avatar',
          width: '36px'
        },
        {
          text: this.$t('exoplatform.kudos.label.name'),
          sortable: false,
          value: 'name'
        },
        {
          text: this.$t('exoplatform.kudos.label.received'),
          align: 'center',
          sortable: true,
          value: 'received'
        },
        {
          text: this.$t('exoplatform.kudos.label.sent'),
          align: 'center',
          sortable: true,
          value: 'sent'
        }
      ];
    },
    periods(){
      return [
        {
          text: this.$t('exoplatform.kudos.label.week'),
          value: 'WEEK'
        },
        {
          text: this.$t('exoplatform.kudos.label.month'),
          value: 'MONTH'
        },
        {
          text: this.$t('exoplatform.kudos.label.quarter'),
          value: 'QUARTER'
        },
        {
          text: this.$t('exoplatform.kudos.label.semester'),
          value: 'SEMESTER'
        },
        {
          text: this.$t('exoplatform.kudos.label.year'),
          value: 'YEAR'
        }
      ];
    },
    selectedStartDate() {
      return (this.selectedStartDateInSeconds && this.formatDate(new Date(this.selectedStartDateInSeconds * 1000), this.lang)) || 0;
    },
    selectedEndDate() {
      return (this.selectedEndDateInSeconds && this.formatDate(new Date(this.selectedEndDateInSeconds * 1000), this.lang)) || 0;
    },
    periodDatesDisplay() {
      if (this.selectedStartDate && this.selectedEndDate) {
        return `${this.selectedStartDate} ${this.$t('exoplatform.kudos.label.to')} ${this.selectedEndDate}`;
      } else if (this.selectedStartDate) {
        return this.selectedStartDate;
      } else {
        return '';
      }
    },
    selectedPeriodType() {
      const selectedPeriodType = this.periods.find(period => period.value === this.kudosPeriodType);
      return selectedPeriodType ? selectedPeriodType.text : this.kudosPeriodType;
    }
  },
  watch: {
    selectedDate() {
      this.loadAll();
    },
    kudosPeriodType() {
      this.loadAll();
    },
    defaultKudosPeriodType() {
      if (!this.kudosPeriodType && this.defaultKudosPeriodType) {
        this.selectedDate = new Date().toISOString().substr(0, 10);
      }
      this.kudosPeriodType = this.defaultKudosPeriodType;
    }
  },
  created() {
    document.addEventListener('exo-kudos-get-kudos-list-loading', () => this.loading = true);
    document.addEventListener('exo-kudos-get-kudos-list-result', this.loadKudosList);
    document.addEventListener('exo-kudos-get-period-result', this.loadPeriodDates);

    this.lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language;
  },
  methods: {
    loadAll() {
      if (!this.selectedDate || !this.kudosPeriodType) {
        return;
      }
      this.selectedStartDateInSeconds = new Date(this.selectedDate).getTime() / 1000;
      this.selectedEndDateInSeconds = null;
      document.dispatchEvent(new CustomEvent('exo-kudos-get-period', {'detail': {'date': new Date(this.selectedDate), 'periodType': this.kudosPeriodType}}));
    },
    loadPeriodDates(event) {
      if (event && event.detail && event.detail.period) {
        this.selectedStartDateInSeconds = event.detail.period.startDateInSeconds;
        this.selectedEndDateInSeconds = event.detail.period.endDateInSeconds;
        document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list', {'detail': {'startDate': new Date(this.selectedStartDateInSeconds * 1000), 'endDate': new Date(this.selectedEndDateInSeconds * 1000)}}));
      } else {
        console.error('Retrieved event detail doesn\'t have the period as result');
      }
    },
    loadKudosList(event) {
      this.error = null;
      this.kudosIdentitiesList = [];
      if (!event || !event.detail) {
        this.error = this.$t('exoplatform.kudos.warning.emptyKudosList');
      } else if (event.detail.error) {
        console.error(event.detail.error);
        this.error = event.detail.error;
      } else if (event.detail.list) {
        this.kudosIdentitiesList = event.detail.list;
      } else {
        this.error = this.$t('exoplatform.kudos.warning.emptyKudosList');
      }
      this.loading = false;
    },
    formatDate(date) {
      if (!date){
        return null;
      }
      return date.toLocaleDateString(eXo.env.portal.language);
    }
  }
};
</script>