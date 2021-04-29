export function sendKudos(kudo, limit) {
  if (kudo) {
    return fetch(`/portal/rest/kudos/api/kudos?limit=${limit || 0}`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(kudo),
    }).then((resp) => resp && resp.ok);
  } else {
    return Promise.resolve(null);
  }
}

export function getKudosSent(senderIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/portal/rest/kudos/api/kudos/${senderIdentityId}/sent?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function getKudosReceived(receiverIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/portal/rest/kudos/api/kudos/${receiverIdentityId}/received?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function getEntityKudos(entityType, entityId, limit) {
  if (entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/byEntity?entityId=${entityId}&entityType=${entityType}&limit=${limit || 0}`, {
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }).then((resp) => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve([]);
  }
}

export function getKudosByPeriodOfDate(date, limit) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos?dateInSeconds=${date}&limit=${limit || 0}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function getKudosByPeriod(startDate, endDate, limit) {
  // convert from milliseconds to seconds
  startDate = parseInt(startDate.getTime() / 1000);
  endDate = parseInt(endDate.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/byDates?startDateInSeconds=${startDate}&endDateInSeconds=${endDate}&limit=${limit || 0}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function getPeriodDates(date, periodType) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/period?dateInSeconds=${date}&periodType=${periodType}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function registerExternalExtensions(title) {
  const profileExtensionAction = {
    id: `profile-kudos`,
    title: title,
    icon: 'fa fa-award uiIconKudos uiIconLightBlue',
    order: 20,
    enabled: (profile) => profile.enabled && !profile.deleted,
    click: (profile) => {
      const type = profile.prettyName ? 'SPACE_PROFILE' : 'USER_PROFILE';
      const id = profile.prettyName ? profile.id : profile.username;
      if (id) {
        document.dispatchEvent(
          new CustomEvent('exo-kudos-open-send-modal', {detail: {
            id: id,
            type: type,
          }}));
      }
    },
  };
  extensionRegistry.registerExtension('profile-extension', 'action', profileExtensionAction);
  document.dispatchEvent(new CustomEvent('profile-extension-updated', { detail: profileExtensionAction}));
}

export function registerActivityReactionTabs(activityType, activityId,kudosNumber, kudosList) {
  const activityExtensionReaction = {
    id: `kudos-${activityId}`,
    icon: 'fa fa-award uiIconKudos',
    order: 3,
    activityType: activityType,
    activityId: activityId,
    kudosNumber: kudosNumber,
    reactionListItems: kudosList,
    class: 'Kudos'
  };
  const contentsToLoad = extensionRegistry.loadExtensions('activity-reactions', 'activity-reactions') || [];
  if (!contentsToLoad || !contentsToLoad.length || !contentsToLoad.find(activityReactionPlugin => activityReactionPlugin.id && activityReactionPlugin.id === activityExtensionReaction.id)) {
    extensionRegistry.registerExtension('activity-reactions', 'activity-reactions', activityExtensionReaction);
  }
}


