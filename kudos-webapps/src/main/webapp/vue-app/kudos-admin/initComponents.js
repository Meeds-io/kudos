import KudosAdmin from './components/KudosAdmin.vue';
import KudosList from './components/KudosList.vue';

const components = {
  'kudos-admin': KudosAdmin,
  'kudos-list': KudosList,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
