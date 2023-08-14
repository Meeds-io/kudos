import KudosIdentityLink from './components/KudosIdentityLink.vue';
import KudosAPI from './components/KudosAPI.vue';
import KudosApp from './components/KudosApp.vue';
import KudosButton from '../kudos/components/KudosButton.vue';
import SendKudosComposer from '../kudos/components/SendKudosComposer.vue';
import PopoverKudosButton from '../kudos/components/PopoverKudosButton.vue';
import ActivityKudosReactionItem from './components/ActivityKudosReactionItem.vue';
import ActivityKudosReactionList from './components/ActivityKudosReactionList.vue';
import ActivityKudosReactionEmptyList from './components/common/ActivityKudosReactionEmptyList.vue';
import KudosOverviewDrawer from './components/common/KudosOverviewDrawer.vue';
import KudosOverviewItem from './components/common/KudosOverviewItem.vue';
import ActivityKudosReactionCount from './components/ActivityKudosReactionCount.vue';
import KudosOverview from '../kudos-overview/components/KudosOverview.vue';
import KudosOverviewCard from '../kudos-overview/components/KudosOverviewCard.vue';

const components = {
  'kudos-api': KudosAPI,
  'kudos-app': KudosApp,
  'kudos-identity-link': KudosIdentityLink,
  'kudos-button': KudosButton,
  'popover-kudos-button': PopoverKudosButton,
  'activity-kudos-reaction-item': ActivityKudosReactionItem,
  'activity-kudos-reaction-list': ActivityKudosReactionList,
  'activity-kudos-reaction-empty-list': ActivityKudosReactionEmptyList,
  'kudos-overview-drawer': KudosOverviewDrawer,
  'kudos-overview-item': KudosOverviewItem,
  'activity-kudos-reaction-count': ActivityKudosReactionCount,
  'kudos-overview': KudosOverview,
  'kudos-overview-card': KudosOverviewCard,
  'send-kudos-composer': SendKudosComposer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

