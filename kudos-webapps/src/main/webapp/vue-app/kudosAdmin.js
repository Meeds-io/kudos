import KudosAdminApp from './components/KudosAdminApp.vue';
import './../css/main.less';

Vue.use(Vuetify);

$(document).ready(() => {
  const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
  const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.Kudos-${lang}.json`;

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    new Vue({
      el: '#KudosAdminApp',
      render: (h) => h(KudosAdminApp),
      i18n,
    })
  });
});
