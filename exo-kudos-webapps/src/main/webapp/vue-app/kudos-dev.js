import $ from 'jquery';

import KudosApp from './components/KudosApp.vue';
import './../css/main.less';

window.$ = $;
window.parentToWatch = '#test';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosApp',
  render: h => h(KudosApp)
});
