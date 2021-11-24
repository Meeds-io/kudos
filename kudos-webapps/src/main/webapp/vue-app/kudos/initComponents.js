import KudosIdentityLink from './components/KudosIdentityLink.vue';
import KudosAPI from './components/KudosAPI.vue';
import KudosApp from './components/KudosApp.vue';
import KudosButton from '../kudos/components/KudosButton.vue';
import KudosNotificationsAlert from './components/common/KudosNotificationsAlert.vue';
import ActivityKudosReactionItem from './components/ActivityKudosReactionItem.vue';
import ActivityKudosReactionList from './components/ActivityKudosReactionList.vue';
import ActivityKudosReactionEmptyList from './components/common/ActivityKudosReactionEmptyList.vue';

const components = {
  'kudos-api': KudosAPI,
  'kudos-app': KudosApp,
  'kudos-identity-link': KudosIdentityLink,
  'kudos-button': KudosButton,
  'kudos-notification-alert': KudosNotificationsAlert,
  'activity-kudos-reaction-item': ActivityKudosReactionItem,
  'activity-kudos-reaction-list': ActivityKudosReactionList,
  'no-kudos-yet': ActivityKudosReactionEmptyList
};

for (const key in components) {
  Vue.component(key, components[key]);
}

