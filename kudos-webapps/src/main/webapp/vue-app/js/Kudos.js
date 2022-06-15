const kudosListByActivity = {};

export function resetActivityKudosList(activity) {
  delete activity.kudosList;
  delete kudosListByActivity[activity.id];
}

export function computeActivityKudosList(activity) {
  const activityKudosListPromise = kudosListByActivity[activity.id] = (activity && activity.kudosList && Promise.resolve(activity.kudosList)) || getKudosListByActivity(activity.id);
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
    return fetch('/portal/rest/kudos/api/kudos', {
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

export function getKudosReceived(receiverIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/portal/rest/kudos/api/kudos/${receiverIdentityId}/received?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
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

export function getKudosListByActivity(activityId) {
  return fetch(`/portal/rest/kudos/api/kudos/byActivity/${activityId}/all`, {
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

export function getEntityKudos(entityType, entityId, limit) {
  if (entityType && entityId) {
    return fetch(`/portal/rest/kudos/api/kudos/byEntity?entityId=${entityId}&entityType=${entityType}&limit=${limit || 0}`, {
      method: 'GET',
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
    method: 'GET',
    credentials: 'include',
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

export function getKudosByPeriod(startDate, endDate, limit) {
  // convert from milliseconds to seconds
  startDate = parseInt(startDate.getTime() / 1000);
  endDate = parseInt(endDate.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/byDates?startDateInSeconds=${startDate}&endDateInSeconds=${endDate}&limit=${limit || 0}`, {
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

export function getPeriodDates(date, periodType) {
  // convert from milliseconds to seconds
  date = parseInt(date.getTime() / 1000);
  return fetch(`/portal/rest/kudos/api/kudos/period?dateInSeconds=${date}&periodType=${periodType}`, {
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

export function registerExternalExtensions(title) {
  const profileExtensionAction = {
    id: 'profile-kudos',
    title: title,
    icon: 'fa fa-award uiIconKudos uiIconLightBlue',
    class: 'fas fa-award',
    additionalClass: 'mt-1',
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

export function registerActivityReactionTabs() {
  extensionRegistry.registerComponent('ActivityReactions', 'activity-reaction-action', {
    id: 'kudos',
    reactionLabel: 'exoplatform.kudos.label.kudos',
    numberOfReactions: 0,
    vueComponent: Vue.options.components['activity-kudos-reaction-list'],
    rank: 2,
  });
  extensionRegistry.registerComponent('ActivityReactionsCount', 'activity-reaction-count', {
    id: 'kudos',
    vueComponent: Vue.options.components['activity-kudos-reaction-count'],
    rank: 1,
  });
}

export function registerActivityActionExtension() {
  extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
    id: 'kudos',
    vueComponent: Vue.options.components['kudos-button'],
    rank: 50,
  });
  extensionRegistry.registerComponent('ActivityCommentFooter', 'activity-footer-comment-action', {
    id: 'kudos',
    vueComponent: Vue.options.components['kudos-button'],
    rank: 50,
  });

  extensionRegistry.registerComponent('UserPopover', 'user-popover-action', {
    id: 'kudos',
    vueComponent: Vue.options.components['popover-kudos-button'],
    rank: 30,
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
      getActivityType: () => 'kudos',
      getTitle: activityOrComment => {
        const kudos = activityOrComment && activityOrComment.kudos;
        if (kudos) {
          const receiverIdentity = {
            'username': kudos.receiverId,
            'fullname': kudos.receiverFullName,
            'position': kudos.receiverPosition,
            'external': kudos.externalReceiver,
          };
          return {
            key: 'NewKudosSentActivityComment.activity_kudos_title',
            params: {
              0: receiverIdentity.fullname
            },
          };
        }
        const title = (activityOrComment.templateParams && activityOrComment.templateParams.kudosMessage) || (activityOrComment.title) || (activityOrComment.body);
        if (title && title.includes('</i>')) {
          return title.split('</i>')[1].split(':')[0] || '';
        }
        return activityOrComment.body;
      },
      getSummary: activityOrComment => {
        const summary = (activityOrComment.templateParams && activityOrComment.templateParams.kudosMessage)
                        || (activityOrComment.kudos && activityOrComment.kudos.message)
                        || '';
        return summary.includes('<oembed>') && summary.split('<oembed>')[0] || summary;
      },
      noTitleEllipsis: true,
      noSummaryEllipsis: true,
      supportsIcon: true,
      useSameViewForMobile: true,
      getDefaultIcon: (activityOrComment) => ({
        icon: 'fa fa-award primary--text',
        size: activityOrComment.activityId && 37 || 72,
        height: 'auto',
        width: activityOrComment.activityId && '30px' || '60px',
        noBorder: true,
      }),
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