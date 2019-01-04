export function getReceiver(entityType, entityId) {
  if (entityType === 'USER_PROFILE' || entityType === 'USER_TIPTIP') {
    return getIdentityDetails(entityId, 'user', entityId)
      .then(identityDetails => {
        // Change entity id to use long instead of string
        if(identityDetails) {
          identityDetails.entityId = identityDetails.identityId;
        }
        return identityDetails;
      });
  } else if (entityType === 'SPACE_PROFILE' || entityType === 'SPACE_TIPTIP') {
    return getIdentityDetails(entityId, 'space', entityId)
      .then(identityDetails => {
        // Change entity id to use long instead of string
        if(identityDetails) {
          identityDetails.entityId = identityDetails.identityId;
        }
        return identityDetails;
      });
  } else if (entityType === 'ACTIVITY' || entityType === 'COMMENT') {
    let ownerId;
    let ownerType;
    let isSpace = false;
    if(entityType === 'COMMENT') {
      entityId = `comment${entityId}`;
    }
    return getActivityDetails(entityId)
      .then(activityDetails => {
        if (activityDetails && activityDetails.owner && activityDetails.owner.href) {
          isSpace = activityDetails.owner.href.indexOf('/spaces/') >= 0;
          ownerType = isSpace ? 'space' : 'user';
          let remoteId = activityDetails.owner.href.substring(activityDetails.owner.href.lastIndexOf('/') + 1);
          ownerId = remoteId;
          return getIdentityDetails(ownerId, ownerType, remoteId);
        } else {
          throw new Error("Uknown activity details", activityDetails);
        }
      })
      .catch(e => {
        console.debug("Error retrieving activity details with id", entityId, e);
      });
  } else {
    console.error("Unkown entity type", entityType, entityId);
  }
}

export function getIdentityDetails(urlId, type, remoteId) {
  const ownerDetails = {
    type: type,
    id: remoteId
  };

  // check if user is authorized to receive Kudos
  return (
          window.kudosSettings.accessPermission && type === 'user' ?
            fetch(`/portal/rest/kudos/api/account/isAuthorized?username=${urlId}`, {credentials: 'include'})
            : Promise.resolve({ok: true})
         )
    .then(resp => {
      if (!resp || !resp.ok) {
        ownerDetails.notAuthorized = true;
        throw new Error();
      }
      if(type === 'user') {
        return fetch(`/portal/rest/v1/social/users/${urlId}`, {credentials: 'include'})
          .then(resp => resp && resp.ok && resp.json())
          .then(identityDetails => {
            if (identityDetails) {
              ownerDetails.id = identityDetails.username;
              ownerDetails.identityId = identityDetails.id;
              ownerDetails.fullname = identityDetails.fullname;
            } else {
              ownerDetails.notAuthorized = true;
            }
            return ownerDetails;
          });
      } else {
        return fetch(`/portal/rest/v1/social/spaces/${urlId}`, {credentials: 'include'})
        .then(resp => resp && resp.ok && resp.json())
        .then(identityDetails => {
          if (identityDetails) {
            ownerDetails.identityId = identityDetails.id;
            ownerDetails.fullname = identityDetails.displayName;
          } else {
            ownerDetails.notAuthorized = true;
          }
          return ownerDetails;
        });
      }
    })
    .catch(e => {
      return ownerDetails;
    });
}

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
