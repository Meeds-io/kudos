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
 * Return an Array of users and spaces that matches the filter (used in suggestion) :
 * {
 *  name: Full name,
 *  id: id,
 *  avatar: Avatar URL/URI
 * }
 */
export function searchContact(filter) {
  let items = null;
  return searchUsers(filter)
    .then(users => items = users && users.length ? users : [])
    .then(() => searchSpaces(filter))
    .then(spaces => items = items.concat(spaces))
    .catch((e) => {
      console.debug("searchContact method - error", e);
    });
}


/*
 * Search users from eXo Platform, used for suggester
 */
export function searchUsers(filter, includeCurrentUserInResults) {
  const params = $.param({
    nameToSearch: filter,
    typeOfRelation: 'mention_activity_stream',
    currentUser: includeCurrentUserInResults ? '' : eXo.env.portal.userName,
    spaceURL: isOnlySpaceMembers() ? getAccessPermission() : null
  });
  return fetch(`/portal/rest/social/people/suggest.json?${params}`, {credentials: 'include'})
    .then(resp =>  {
      if (resp.ok) {
        return resp.json();
      } else {
        return null;
      }
    })
    .then(items => {
      if (items) {
        if (items.options) {
          items = items.options;
        }
        items.forEach((item) => {
          if (item.id && item.id.indexOf('@') === 0) {
            item.id = item.id.substring(1);
            item.id_type = `user_${item.id}`;
            if (!item.avatar) {
              item.avatar = item.avatarUrl ? item.avatarUrl : `/rest/v1/social/users/${item.id}/avatar`;
            }
          }
        });
      } else {
        items = [];
      }
      return items;
    });
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

/*
 * Return the user or space object
 * {
 *  "name": display name of space of user,
 *  "id": Id of space of user,
 *  "avatar": avatar URL/URI,
 *  "type": 'user' or 'space',
 *  "creator": space creator username for space type
 * }
 */
export function searchUserOrSpaceObject(id, type) {
  return fetch(`/portal/rest/kudos/api/account/detailsById?id=${id}&type=${type}`, {credentials: 'include'})
    .then(resp =>  {
      if (resp.ok) {
        return resp.json();
      } else {
        return null;
      }
    }).then(item =>  {
      if(item && item.id && item.type) {
        item.id_type = `${item.type}_${item.id}`;
      }
      return item;
    });
}
