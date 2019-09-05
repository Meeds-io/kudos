import KudosApp from './components/KudosApp.vue';
import './../css/main.less';

window.parentToWatch = '#UISpaceActivityStreamPortlet, #UIUserActivityStreamPortlet';

Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: 'mdi',
});

const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.Kudos-${lang}.json`;

exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  new Vue({
    render: (h) => h(KudosApp),
    i18n,
    vuetify,
  }).$mount('#KudosApp');
});
