import KudosOverview from './components/KudosOverview.vue';
import KudosOverviewCard from './components/KudosOverviewCard.vue';
import KudosOverviewRow from './components/KudosOverviewRow.vue';

const components = {
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
  'kudos-overview-row': KudosOverviewRow,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
