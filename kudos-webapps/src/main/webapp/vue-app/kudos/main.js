import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('KudosAdmin');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

import * as kudosService from '../js/Kudos.js';
if (!Vue.prototype.$kudosService) {
  window.Object.defineProperty(Vue.prototype, '$kudosService', {
    value: kudosService,
  });
}

window.parentToWatch = '#UISpaceActivityStreamPortlet, #UIUserActivityStreamPortlet';

Vue.use(Vuetify);

const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'KudosApp';

//should expose the locale ressources as REST API
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `/kudos/i18n/locale.addon.Kudos?lang=${lang}`;

exoi18n.loadLanguageAsync(lang, url).then(i18n => {
// init Vue app when locale ressources are ready
  new Vue({
    template: `<kudos-app id="${appId}" />`,
    i18n,
    vuetify,
  }).$mount(`#${appId}`);
});