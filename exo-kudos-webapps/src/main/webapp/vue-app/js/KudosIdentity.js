export function getActivityDetails(activityId) {
  if(activityId) {
    return fetch(`/portal/rest/v1/social/activities/${activityId}`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })
      .then(resp => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve(null);
  }
}

export function getReceiver(entityType, entityId) {
  if (entityType === 'ACTIVITY') {
    return getActivityDetails(entityId)
      .then(activityDetails => {
        // TODO workaround for SOC-6128 to get receiver details
        // which is not convenient because we can't retrieve
        // Space display name with space identity URL
        // (when fixed, we can user activityDetails.owner.href)
        if (activityDetails && activityDetails.identity) {
          return fetch(activityDetails.identity, {credentials: 'include'});
        } else {
          throw new Error("Uknown activity details", activityDetails);
        }
      })
      .then(resp => resp && resp.ok && resp.json())
      .then(ownerDetails => {
        if(ownerDetails
            && ownerDetails.providerId
            && ownerDetails.globalId
            && ownerDetails.globalId.localId) {
          return {
            id: ownerDetails.globalId.localId,
            type: ownerDetails.providerId,
            fullname: (ownerDetails.profile && ownerDetails.profile.fullname) || ownerDetails.globalId.localId
          };
        } else {
          throw new Error("Owner details not found", ownerDetails);
        }
      })
      .catch(e => {
        console.debug("Error retrieving activity details with id", activityId, e);
      });
  }
}

export function getEntityKudos(entityType, entityId) {
  if(entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/getEntityKudos?entityId=${entityId}&entityType=${entityType}`, {
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })
      .then(resp => resp && resp.ok && resp.json());
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
    })
    .then(resp => resp && resp.ok);
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
    })
    .then(resp => resp && resp.ok && resp.json());
  } else {
    return Promise.resolve(null);
  }
}

/*
 * Search spaces from eXo Platform, used for suggester
 */
export function searchSpaces(filter) {
  const params = $.param({fields: ["id","prettyName","displayName","avatarUrl"], keyword: filter});
  return fetch(`/portal/rest/space/user/searchSpace?${params}`, {credentials: 'include'})
    .then(resp =>  {
      if (resp.ok) {
        return resp.json();
      } else {
        return null;
      }
    })
    .then(items => {
      const result = [];
      items.forEach((item) => {
        result.push({
          avatar: item.avatarUrl ? item.avatarUrl : `/portal/rest/v1/social/spaces/${item.prettyName}/avatar`,
          name: item.displayName,
          id: item.prettyName,
          id_type: `space_${item.prettyName}`
        });
      });
      return result;
    });
}
