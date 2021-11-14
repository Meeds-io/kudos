import KudosIdentityLink from './components/KudosIdentityLink.vue';
import KudosAPI from './components/KudosAPI.vue';
import KudosApp from './components/KudosApp.vue';
import KudosButton from '../kudos/components/KudosButton.vue';
import KudosNotificationsAlert from './components/common/KudosNotificationsAlert.vue';
import ActivityKudosItem from './components/ActivityKudosItem.vue';
import ActivityKudosList from './components/ActivityKudosList.vue';

const components = {
  'kudos-api': KudosAPI,
  'kudos-app': KudosApp,
  'kudos-identity-link': KudosIdentityLink,
  'kudos-button': KudosButton,
  'kudos-notification-alert': KudosNotificationsAlert,
  'activity-kudos-item': ActivityKudosItem,
  'activity-kudos-list': ActivityKudosList
};

for (const key in components) {
  Vue.component(key, components[key]);
}

