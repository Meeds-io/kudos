import KudosApp from './components/KudosApp.vue';
import './../css/main.less';

window.parentToWatch = '#UISpaceActivityStreamPortlet, #UIUserActivityStreamPortlet';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#KudosApp',
  render: h => h(KudosApp)
});
