import KudosActivityApp from './components/KudosActivityApp.vue';
import './../css/main.less';

window.parentToWatch = '#UISpaceActivityStreamPortlet, #UIUserActivityStreamPortlet';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosActivityApp',
  render: h => h(KudosActivityApp)
});
