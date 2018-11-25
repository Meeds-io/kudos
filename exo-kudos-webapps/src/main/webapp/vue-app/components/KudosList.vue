<template>
  <v-flex>
    <v-menu
      ref="selectedDateMenu"
      v-model="selectedDateMenu"
      transition="scale-transition"
      lazy
      offset-y
      xs12 lg6>
      <v-text-field
        slot="activator"
        v-model="dateFormatted"
        label="Date"
        persistent-hint
        prepend-icon="event"
        @blur="date = parseDate(dateFormatted)" />
      <v-date-picker
        v-model="selectedMonth"
        type="month"
        @input="selectedDateMenu = false" />
    </v-menu>
  
    <v-data-table
      :headers="kudosIdentitiesHeaders"
      :items="kudosIdentitiesList"
      :loading="loading"
      :sortable="true"
      :pagination.sync="pagination"
      class="elevation-1 mr-3 ml-3 mt-2 mb-2"
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
  </v-flex>
</template>

<script>
export default {
  data: vm => {
    return {
      loading: false,
      error: null,
      selectedMonth: new Date().toISOString().substr(0, 7),
      dateFormatted: vm.formatDate(new Date().toISOString().substr(0, 7)),
      selectedDateMenu: false,
      kudosIdentitiesList: [],
      pagination: {
        descending: true
      },
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
  watch: {
    selectedMonth () {
      this.dateFormatted = this.formatDate(this.selectedMonth);
      document.dispatchEvent(new CustomEvent('exo-kudo-get-kudos-list', {'detail' : {'month' : new Date(this.selectedMonth)}}));
    }
  },
  created() {
    document.addEventListener('exo-kudo-get-kudos-list-loading', () => this.loading = true);
    document.addEventListener('exo-kudo-get-kudos-list-result', this.loadKudosList);
    this.$nextTick(() => {
      document.dispatchEvent(new CustomEvent('exo-kudo-get-kudos-list', {'detail' : {'month' : new Date(this.selectedMonth)}}));
    });
  },
  methods: {
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
    formatDate (date) {
      if (!date){
        return null;
      }
      const [year, month] = date.split('-');
      return `${month}/${year}`;
    },
    parseDate (date) {
      if (!date) {
        return null;
      }
      const [month, year] = date.split('/');
      return `${year}-${month.padStart(2, '0')}`;
    }
  }
};
</script>