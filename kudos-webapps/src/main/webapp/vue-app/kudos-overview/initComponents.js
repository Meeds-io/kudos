import KudosOverview from './components/KudosOverview.vue';
import KudosOverviewCard from './components/KudosOverviewCard.vue';
import KudosOverviewRow from './components/KudosOverviewRow.vue';
import KudosOverviewSettingsDrawer from './components/KudosOverviewSettingsDrawer.vue';

const components = {
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
  'kudos-overview-row': KudosOverviewRow,
  'kudos-overview-settings-drawer': KudosOverviewSettingsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
