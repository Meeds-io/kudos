import KudosOverview from './components/KudosOverview.vue';
import KudosOverviewCard from './components/KudosOverviewCard.vue';
import KudosOverviewDrawer from './components/KudosOverviewDrawer.vue';
import KudosOverviewItem from './components/KudosOverviewItem.vue';

const components = {
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
  'kudos-overview-drawer': KudosOverviewDrawer,
  'kudos-overview-item': KudosOverviewItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
