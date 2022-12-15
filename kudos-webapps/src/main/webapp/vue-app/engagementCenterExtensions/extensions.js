const kudosUserActions = ['receiveKudos', 'sendKudos'];
export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'kudos',
    options: {
      rank: 40,
      icon: 'fas fa-award',
      match: (actionLabel) => kudosUserActions.includes(actionLabel),
      getLabel: () => ''
    },
  });
}