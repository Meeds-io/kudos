export function getReceiver(entityType, entityId) {
  if (entityType === 'USER_PROFILE' || entityType === 'USER_TIPTIP' || entityType === 'user') {
    return getIdentityDetails(entityId, 'user', entityId).then((identityDetails) => {
      // Change entity id to use long instead of string
      if (identityDetails) {
        identityDetails.entityId = identityDetails.identityId;
      }
      return identityDetails;
    });
  } else if (entityType === 'SPACE_PROFILE' || entityType === 'SPACE_TIPTIP' || entityType === 'space') {
    return getIdentityDetails(entityId, 'space', entityId).then((identityDetails) => {
      // Change entity id to use long instead of string
      if (identityDetails) {
        identityDetails.entityId = identityDetails.identityId;
      }
      return identityDetails;
    });
  } else if (entityType === 'ACTIVITY' || entityType === 'COMMENT') {
    let ownerId;
    let ownerType;
    let isSpace = false;
    if (entityType === 'COMMENT') {
      entityId = `comment${entityId}`;
    }
    return getActivityDetails(entityId)
      .then(async (activityDetails) => {
        if (activityDetails?.type === 'exokudos:activity') {
          activityDetails.kudos = await getKudosByActivity(entityId);
        }
        const kudosReceiverId = activityDetails?.kudos?.receiverId;
        if (kudosReceiverId) {
          const kudosReceiverType = activityDetails?.kudos?.receiverType;
          return getIdentityDetails(kudosReceiverId, kudosReceiverType, kudosReceiverId);
        } else if (activityDetails?.owner?.href) {
          isSpace = activityDetails.owner.href.indexOf('/spaces/') >= 0;
          ownerType = isSpace ? 'space' : 'user';
          const remoteId = activityDetails.owner.href.substring(activityDetails.owner.href.lastIndexOf('/') + 1);
          ownerId = remoteId;
          return getIdentityDetails(ownerId, ownerType, remoteId);
        } else {
          throw new Error('Uknown activity details', activityDetails);
        }
      })
      .catch((e) => {
        console.error('Error retrieving activity details with id', entityId, e);
      });
  } else {
    return Promise.resolve();
  }
}

export function getIdentityDetails(urlId, type, remoteId) {
  const ownerDetails = {
    type: type,
    id: remoteId,
  };

  // check if user is authorized to receive Kudos
  return (window.kudosSettings.accessPermission && type === 'user' ? fetch(`/kudos/rest/account/isAuthorized?username=${urlId}`, {credentials: 'include'}) : Promise.resolve({ok: true}))
    .then((resp) => {
      if (!resp || !resp.ok) {
        ownerDetails.notAuthorized = true;
        throw new Error();
      }
      if (type === 'user') {
        return fetch(`/portal/rest/v1/social/users/${urlId}`, {credentials: 'include'})
          .then((resp) => resp && resp.ok && resp.json())
          .then((identityDetails) => {
            if (identityDetails) {
              ownerDetails.id = identityDetails.username;
              ownerDetails.identityId = identityDetails.id;
              ownerDetails.fullname = identityDetails.fullname;
              ownerDetails.avatar = identityDetails.avatar;
              ownerDetails.username = identityDetails.username;
              ownerDetails.position = identityDetails.position;
              ownerDetails.external = identityDetails.external === 'true';
              ownerDetails.enabled = identityDetails.enabled;
            } else {
              ownerDetails.notAuthorized = true;
            }
            return ownerDetails;
          });
      } else {
        const url = Number.isFinite(Number(urlId)) ? `/portal/rest/v1/social/spaces/${urlId}` : `/portal/rest/v1/social/spaces/byPrettyName/${urlId}`;
        return fetch(url, {credentials: 'include'})
          .then((resp) => resp && resp.ok && resp.json())
          .then((identityDetails) => {
            if (identityDetails) {
              ownerDetails.identityId = identityDetails.id;
              ownerDetails.fullname = identityDetails.displayName;
              ownerDetails.avatar = identityDetails.avatarUrl;
              ownerDetails.external = false;
              ownerDetails.enabled = true;
            } else {
              ownerDetails.notAuthorized = true;
            }
            return ownerDetails;
          });
      }
    })
    .catch((e) => {
      console.error('Error identity details with remoteId', remoteId, e);
      return ownerDetails;
    });
}

export function getActivityDetails(activityId) {
  if (activityId) {
    return fetch(`/portal/rest/v1/social/activities/${activityId}`, {
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

export function getKudosByActivity(activityId) {
  return fetch(`/kudos/rest/kudos/byActivity/${activityId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

/*
 * Search spaces from eXo Platform, used for suggester
 */
export function searchSpaces(filter) {
  const params = $.param({fields: ['id', 'prettyName', 'displayName', 'avatarUrl'], keyword: filter});
  return fetch(`/portal/rest/space/user/searchSpace?${params}`, {credentials: 'include'})
    .then((resp) => {
      if (resp.ok) {
        return resp.json();
      } else {
        return null;
      }
    })
    .then((items) => {
      const result = [];
      items.forEach((item) => {
        result.push({
          avatar: item.avatarUrl ? item.avatarUrl : `/portal/rest/v1/social/spaces/${item.prettyName}/avatar`,
          name: item.displayName,
          id: item.prettyName,
          id_type: `space_${item.prettyName}`,
        });
      });
      return result;
    });
}

