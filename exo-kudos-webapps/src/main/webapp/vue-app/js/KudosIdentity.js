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
    let ownerIdentityId;
    return getActivityDetails(entityId)
      .then(activityDetails => {
        if (activityDetails && activityDetails.owner && activityDetails.owner.href) {
          ownerIdentityId = activityDetails.identity.substring(activityDetails.identity.lastIndexOf('/') + 1);
          return fetch(activityDetails.owner.href, {credentials: 'include'});
        } else {
          throw new Error("Uknown activity details", activityDetails);
        }
      })
      .then(resp => resp && resp.ok && resp.json())
      .then(ownerDetails => {
        if(ownerDetails) {
          const isSpace = ownerDetails.subscription;
          return {
            id: isSpace ? ownerDetails.groupId && ownerDetails.groupId.replace('/spaces/', '') : ownerDetails.username,
            type: isSpace ? 'space' : 'user',
            identityId: ownerIdentityId,
            fullname: isSpace ? ownerDetails.displayName : ownerDetails.fullname
          };
        } else {
          throw new Error("Owner details not found", ownerDetails);
        }
      })
      .catch(e => {
        console.debug("Error retrieving activity details with id", entityId, e);
      });
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
