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

const appId = 'KudosOverview';

//should expose the locale ressources as REST API 
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `/kudos/i18n/locale.addon.Kudos?lang=${lang}`;

export function init(
  portletStorageId,
  kudosPeriod,
  canEdit,
  pageRef) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        data: {
          portletStorageId,
          kudosPeriod,
          canEdit,
          pageRef
        },
        mounted() {
          this.$root.$applicationLoaded();
        },
        template: `<kudos-overview id="${appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Kudos Period');
    });
}
