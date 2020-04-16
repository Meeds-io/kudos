export function getEntityKudos(entityType, entityId) {
  if (entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/getEntityKudos?entityId=${entityId}&entityType=${entityType}`, {
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

export function sendKudos(kudo) {
  if (kudo) {
    return fetch(`/portal/rest/kudos/api/kudos/createKudos`, {
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

export function getKudos(userId) {
  if (userId) {
    return fetch(`/portal/rest/kudos/api/kudos/getKudos?senderId=${userId}`, {
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }).then((resp) => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve(null);
  }
}

export function getAllKudosByPeriod(startDate, endDate) {
  // convert from milliseconds to seconds
  startDate = parseInt(startDate.getTime() / 1000);
  endDate = parseInt(endDate.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/getAllKudosByPeriod?startDateInSeconds=${startDate}&endDateInSeconds=${endDate}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function getAllKudosByPeriodOfDate(date) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/getAllKudosByPeriodOfDate?dateInSeconds=${date}`, {
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
  return fetch(`/portal/rest/kudos/api/kudos/getPeriodDates?dateInSeconds=${date}&periodType=${periodType}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => resp && resp.ok && resp.json());
}

export function registerExternalExtensions(title) {
  const profileExtensionAction = {
    title: title,
    icon: 'fa fa-award uiIconKudos uiIconLightBlue',
    order: 20,
    enabled: () => true,
    click: (profile) => {
      document.dispatchEvent(
        new CustomEvent('exo-kudos-open-send-modal', {detail : {
          id : profile.username,
          type: 'USER_PROFILE'
      }}));
    },
  };
  extensionRegistry.registerExtension('profile-extension', 'action', profileExtensionAction);
  document.dispatchEvent(new CustomEvent('profile-extension-updated', { detail: profileExtensionAction}));
}

