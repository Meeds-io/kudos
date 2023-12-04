export function initSettings() {
  return fetch('/portal/rest/kudos/api/account/settings', {
    credentials: 'include'
  })
    .then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      } else {
        return resp.json();
      }
    })
    .then((resp) => resp && resp.ok && resp.json())
    .then((settings) => (window.kudosSettings = settings ? settings : {}))
    .then(() => getSettings())
    .then((settings) => {
      if (settings) {
        window.kudosSettings = {...window.kudosSettings, ...settings};
      }
    });
}

export function getSettings() {
  return fetch('/portal/rest/kudos/api/settings', {
    credentials: 'include',
  })
    .then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      } else {
        return resp.json();
      }
    });
}

export function saveSettings(settings) {
  return fetch('/portal/rest/kudos/api/settings/save', {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(settings),
  })
    .then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      }
    });
}
