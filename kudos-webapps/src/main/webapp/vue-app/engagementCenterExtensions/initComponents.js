import KudosActionValue from './components/KudosActionValue.vue';
const components = {
  'kudos-action-value': KudosActionValue,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

