export function initSettings() {
  return fetch(`/portal/rest/kudos/api/account/settings`, {credentials: 'include'})
    .then(resp =>  resp && resp.ok && resp.json())
    .then(settings =>  {
      window.kudosSettings = settings;
    });
}