import $ from 'jquery';

import KudosActivityApp from './components/KudosActivityApp.vue';
import './../css/main.less';

window.$ = $;
window.parentToWatch = '#test';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosActivityApp',
  render: h => h(KudosActivityApp)
});
