import KudosOverview from './components/KudosOverview.vue';
import KudosOverviewCard from './components/KudosOverviewCard.vue';

const components = {
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
