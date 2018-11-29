<template>
  <v-card flat>
    <v-card-text>
      <v-combobox
        v-model="kudosPeriodType"
        :items="periods"
        :return-object="false"
        label="Period type"
        hide-no-data
        hide-selected
        small-chips
        class="kudosPeriodTypeInput">
        <template slot="selection" slot-scope="data">
          {{ selectedPeriodType }}
        </template>
      </v-combobox>
      <v-menu
        ref="selectedDateMenu"
        v-model="selectedDateMenu"
        transition="scale-transition"
        lazy
        offset-y
        class="kudosDateSelector">
        <v-text-field
          slot="activator"
          v-model="periodDatesDisplay"
          label="Select the period date"
          prepend-icon="event" />
        <v-date-picker
          v-model="selectedDate"
          :first-day-of-week="1"
          :type="!kudosPeriodType || kudosPeriodType === 'WEEK' ? 'date' : 'month'"
          @input="selectedDateMenu = false" />
      </v-menu>
    
      <v-data-table
        :headers="kudosIdentitiesHeaders"
        :items="kudosIdentitiesList"
        :loading="loading"
        :sortable="true"
        :pagination.sync="pagination"
        class="elevation-1 mr-3 mb-2"
        hide-actions>
        <template slot="items" slot-scope="props">
          <tr>
            <td>
              <v-avatar size="36px">
                <img
                  :src="props.item.avatar"
                  onerror="this.src = '/eXoSkin/skin/images/system/SpaceAvtDefault.png'">
              </v-avatar>
            </td>
            <td class="text-xs-left">
              <a :href="props.item.url" rel="nofollow" target="_blank">{{ props.item.name }}</a>
            </td>
            <td v-html="props.item.received">
            </td>
            <td v-html="props.item.sent">
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
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
      selectedStartDate: null,
      selectedEndDate: null,
      pagination: {
        descending: true
      },
      periods: [
        {
          text: 'Week',
          value: 'WEEK'
        },
        {
          text: 'Month',
          value: 'MONTH'
        },
        {
          text: 'Quarter',
          value: 'QUARTER'
        },
        {
          text: 'Semester',
          value: 'SEMESTER'
        },
        {
          text: 'Year',
          value: 'YEAR'
        }
      ],
      kudosIdentitiesHeaders: [
        {
          text: '',
          align: 'right',
          sortable: false,
          value: 'avatar',
          width: '36px'
        },
        {
          text: 'Name',
          align: 'left',
          sortable: false,
          value: 'name'
        },
        {
          text: 'Received',
          align: 'center',
          sortable: true,
          value: 'received'
        },
        {
          text: 'Sent',
          align: 'center',
          sortable: true,
          value: 'sent'
        }
      ]
    };
  },
  computed: {
    periodDatesDisplay() {
      if(this.selectedStartDate && this.selectedEndDate) {
        return `${this.selectedStartDate} to ${this.selectedEndDate}`;
      } else if(this.selectedStartDate) {
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
      if(!this.kudosPeriodType && this.defaultKudosPeriodType) {
        this.selectedDate = new Date().toISOString().substr(0, 10);
      }
      this.kudosPeriodType = this.defaultKudosPeriodType;
    }
  },
  created() {
    document.addEventListener('exo-kudos-get-kudos-list-loading', () => this.loading = true);
    document.addEventListener('exo-kudos-get-kudos-list-result', this.loadKudosList);
    document.addEventListener('exo-kudos-get-period-result', this.loadPeriodDates);
  },
  methods: {
    loadAll() {
      if (!this.selectedDate || !this.kudosPeriodType) {
        return;
      }
      this.selectedStartDate = this.formatDate(new Date(this.selectedDate));
      this.selectedEndDate = null;
      document.dispatchEvent(new CustomEvent('exo-kudos-get-period', {'detail' : {'date' : new Date(this.selectedDate), 'periodType': this.kudosPeriodType}}));
    },
    loadPeriodDates(event) {
      if(event && event.detail && event.detail.period) {
        this.selectedStartDate = this.formatDate(new Date(event.detail.period.startDateInSeconds * 1000));
        this.selectedEndDate = this.formatDate(new Date(event.detail.period.endDateInSeconds * 1000));
        document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list', {'detail' : {'startDate' : new Date(this.selectedStartDate), 'endDate' : new Date(this.selectedEndDate)}}));
      } else {
        console.debug("Retrieved event detail doesn't have the period as result");
      }
    },
    loadKudosList(event) {
      this.error = null;
      this.kudosIdentitiesList = [];
      if(!event || !event.detail) {
        this.error = 'Empty kudos list is retrieved';
      } else if(event.detail.error) {
        console.debug(event.detail.error);
        this.error = event.detail.error;
      } else if(event.detail.list) {
        this.kudosIdentitiesList = event.detail.list;
      } else {
        this.error = 'Empty kudos list is retrieved';
      }
      this.loading = false;
    },
    formatDate(date) {
      if (!date){
        return null;
      }
      const dateString = date.toString();
      // Example: 'Feb 01 2018'
      return dateString.substring(dateString.indexOf(' ') + 1, dateString.indexOf(":") - 3);
    }
  }
};
</script>