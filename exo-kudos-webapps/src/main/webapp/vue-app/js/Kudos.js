export function getEntityKudos(entityType, entityId) {
  if(entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/getEntityKudos?entityId=${entityId}&entityType=${entityType}`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }).then(resp => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve([]);
  }
}

export function sendKudos(kudo) {
  if(kudo) {
    return fetch(`/portal/rest/kudos/api/kudos/saveKudos`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(kudo)
    }).then(resp => resp && resp.ok);
  } else {
    return Promise.resolve(null);
  }
}

export function getKudos(userId) {
  if(userId) {
    return fetch(`/portal/rest/kudos/api/kudos/getKudos?identityId=${userId}`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }).then(resp => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve(null);
  }
}

export function getKudosByMonth(month) {
  // convert from milliseconds to seconds
  month = parseInt(month.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/getKudosByMonth?month=${month}`, {
    credentials: 'include',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  }).then(resp => resp && resp.ok && resp.json());
}
