export function initSettings() {
  return fetch('/kudos/rest/account/settings', {credentials: 'include'})
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
  return fetch('/kudos/rest/settings', {credentials: 'include'}).then((resp) => resp && resp.ok && resp.json());
}

export function saveSettings(settings) {
  if (settings) {
    return fetch('/kudos/rest/settings', {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(settings),
    }).then((resp) => resp && resp.ok);
  } else {
    return Promise.resolve(null);
  }
}
