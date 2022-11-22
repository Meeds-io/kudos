import './initComponents.js';
const kudosUserActions = ['receiveKudos', 'sendKudos'];
export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'kudos',
    options: {
      rank: 40,
      vueComponent: Vue.options.components['kudos-action-value'],
      match: (actionLabel) => kudosUserActions.includes(actionLabel),
    },
  });
}