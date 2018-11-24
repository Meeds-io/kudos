import KudosAdminApp from './components/KudosAdminApp.vue';
import './../css/main.less';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosAdminApp',
  render: h => h(KudosAdminApp)
});
