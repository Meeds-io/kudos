const kudosListByActivity = {};

export function computeActivityKudosList(activity) {
  const activityKudosListPromise = kudosListByActivity[activity.id] = getKudosListByActivity(activity.id);
  return activityKudosListPromise
    .then(kudosList => {
      kudosListByActivity[activity.id] = kudosList;
      const activityId = Number(activity.id);
      activity.kudos = kudosList.find(kudosTmp => kudosTmp.activityId === activityId);
      activity.linkedKudosList = kudosList.filter(kudosTmp => kudosTmp.entityId === activity.id);
      return kudosList;
    });
}

export function computeCommentKudosList(activity, comment) {
  const activityKudosList = kudosListByActivity[activity.id];
  if (!activityKudosList) {
    return computeActivityKudosList(activity).then(() => computeCommentKudosList(activity, comment));
  } else {
    const activityKudosListPromise = activityKudosList.then ? activityKudosList : Promise.resolve(activityKudosList);
    return activityKudosListPromise
      .then(kudosList => {
        comment.kudos = kudosList.find(kudosTmp => kudosTmp.activityId === comment.id || `comment${kudosTmp.activityId}` === comment.id);
        comment.linkedKudosList = kudosList.filter(kudosTmp => kudosTmp.entityId === comment.id || `comment${kudosTmp.entityId}` === comment.id);
        return kudosList;
      });
  }
}

export function sendKudos(kudo) {
  if (kudo) {
    return fetch(`/portal/rest/kudos/api/kudos`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(kudo),
    }).then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      } else {
        return resp.json();
      }
    });
  } else {
    return Promise.resolve(null);
  }
}

export function getKudosSent(senderIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/portal/rest/kudos/api/kudos/${senderIdentityId}/sent?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getKudosReceived(receiverIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/portal/rest/kudos/api/kudos/${receiverIdentityId}/received?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getKudosListByActivity(activityId) {
  return fetch(`/portal/rest/kudos/api/kudos/byActivity/${activityId}/all`, {
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getEntityKudos(entityType, entityId, limit) {
  if (entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/byEntity?entityId=${entityId}&entityType=${entityType}&limit=${limit || 0}`, {
      credentials: 'include',
    }).then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      } else {
        return resp.json();
      }
    });
  } else {
    return Promise.resolve([]);
  }
}

export function countUserKudosSentByEntity(entityType, entityId) {
  return fetch(`/portal/rest/kudos/api/kudos/byEntity/sent/count?entityId=${entityId}&entityType=${entityType}`, {
    credentials: 'include',
    method: 'GET',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.text();
    }
  });
}

export function getKudosByPeriodOfDate(date, limit) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos?dateInSeconds=${date}&limit=${limit || 0}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getKudosByPeriod(startDate, endDate, limit) {
  // convert from milliseconds to seconds
  startDate = parseInt(startDate.getTime() / 1000);
  endDate = parseInt(endDate.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/byDates?startDateInSeconds=${startDate}&endDateInSeconds=${endDate}&limit=${limit || 0}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getPeriodDates(date, periodType) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/period?dateInSeconds=${date}&periodType=${periodType}`, {
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function registerExternalExtensions(title) {
  const profileExtensionAction = {
    id: 'profile-kudos',
    title: title,
    icon: 'fa fa-award uiIconKudos uiIconLightBlue',
    order: 20,
    enabled: (profile) => profile.enabled && !profile.deleted,
    click: (profile) => {
      const type = profile.prettyName ? 'SPACE_PROFILE' : 'USER_PROFILE';
      const id = profile.prettyName ? profile.id : profile.username;
      if (id) {
        document.dispatchEvent(
          new CustomEvent('exo-kudos-open-send-modal', {detail: {
            id: id,
            type: type,
          }}));
      }
    },
  };
  extensionRegistry.registerExtension('profile-extension', 'action', profileExtensionAction);
  document.dispatchEvent(new CustomEvent('profile-extension-updated', { detail: profileExtensionAction}));
}

export function registerActivityReactionTabs(activityType, activityId,kudosNumber, kudosList) {
  const activityExtensionReaction = {
    id: `kudos-${activityId}`,
    icon: 'fa fa-award uiIconKudos',
    order: 3,
    activityType: activityType,
    activityId: activityId,
    kudosNumber: kudosNumber,
    reactionListItems: kudosList,
    class: 'Kudos'
  };
  const contentsToLoad = extensionRegistry.loadExtensions('activity-reactions', 'activity-reactions') || [];
  if (!contentsToLoad || !contentsToLoad.length || !contentsToLoad.find(activityReactionPlugin => activityReactionPlugin.id && activityReactionPlugin.id === activityExtensionReaction.id)) {
    extensionRegistry.registerExtension('activity-reactions', 'activity-reactions', activityExtensionReaction);
  }
}

export function registerActivityActionExtension() {
  extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
    id: 'kudos',
    vueComponent: Vue.options.components['kudos-button'],
    rank: 50,
    init: null,
    isEnabled: params => {
      const activityOwnerId = params && params.activity && params.activity.owner && params.activity.owner.id;
      return activityOwnerId !== eXo.env.portal.userIdentityId;
    }
  });
  extensionRegistry.registerComponent('ActivityCommentFooter', 'activity-footer-comment-action', {
    id: 'kudos',
    vueComponent: Vue.options.components['kudos-button'],
    rank: 50,
    init: null,
  });

  // Register predefined activity types
  extensionRegistry.registerExtension('activity', 'type', {
    type: 'exokudos:activity',
    options: {
      init: (activityOrComment, isActivityDetail,  parentActivity) => {
        const activity = parentActivity || activityOrComment;
        const comment = parentActivity && activityOrComment;
        if (comment) {
          return computeCommentKudosList(activity, comment);
        } else {
          return computeActivityKudosList(activity);
        }
      },
      refresh: (activityOrComment) => {
        const activityId = activityOrComment && activityOrComment.activityId || activityOrComment.id;
        kudosListByActivity[activityId] = null;
      },
      getSourceLink: () => '#',
      getTitle: activityOrComment => {
        const kudos = activityOrComment && activityOrComment.kudos;
        if (kudos) {
          return {
            key: 'NewKudosSentActivityComment.activity_kudos_title',
            params: {
              0: `<a href="${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${kudos.receiverId}">
                    ${kudos.receiverFullName}
                  </a>`
            },
          };
        }
        const title = (activityOrComment.templateParams && activityOrComment.templateParams.kudosMessage) || (activityOrComment.title) || (activityOrComment.body);
        if (title && title.includes('</i>')) {
          return title.split('</i>')[1].split(':')[0] || '';
        }
        return activityOrComment.body;
      },
      getSummary: activityOrComment => 
                  (activityOrComment.templateParams && activityOrComment.templateParams.kudosMessage)
                  || (activityOrComment.kudos && activityOrComment.kudos.message)
                  || activityOrComment.title
                  || activityOrComment.body
                  || '',
      noTitleEllipsis: true,
      noSummaryEllipsis: true,
      supportsIcon: true,
      useSameViewForMobile: true,
      defaultIcon: {
        icon: 'fa fa-award primary--text',
        size: 72,
        height: 'auto',
        width: '60px',
        noBorder: true,
      },
      getBodyToEdit: activityOrComment => {
        if (activityOrComment.templateParams && activityOrComment.templateParams.kudosMessage) {
          return activityOrComment.templateParams.kudosMessage;
        } else {
          return activityOrComment.kudos && activityOrComment.kudos.message || '';
        }
      },
      canEdit: activityOrComment => activityOrComment.identity.id === eXo.env.portal.userIdentityId,
      forceCanEditOverwrite: true,
    },
  });

  document.addEventListener('exo-kudos-sent', (event) => {
    const kudosSent = event && event.detail;
    if (kudosSent) {
      const activityId = kudosSent.parentEntityId || kudosSent.entityId;
      kudosListByActivity[activityId] = null;
      if (kudosSent.entityType === 'ACTIVITY' || kudosSent.entityType === 'COMMENT') { // is a comment
        document.dispatchEvent(new CustomEvent('activity-comment-created', {detail: {
          id: kudosSent.activityId,
          activityId,
        }}));
      }
    }
  });
}