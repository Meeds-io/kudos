import $ from 'jquery';

import KudosAdminApp from './components/KudosAdminApp.vue';
import './../css/main.less';

window.$ = $;

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosAdminApp',
  render: (h) => h(KudosAdminApp),
});
