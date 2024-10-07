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
    return fetch('/kudos/rest/kudos', {
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

export function deleteKudos(kudosId) {
  return fetch(`/kudos/rest/kudos/${kudosId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else if (resp.status === 401) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function getKudosSent(senderIdentityId, limit, returnSize, periodType, dateInSeconds) {
  return fetch(`/kudos/rest/kudos/${senderIdentityId}/sent?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
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
  return fetch(`/kudos/rest/kudos/${receiverIdentityId}/received?limit=${limit || 0}&returnSize=${returnSize || true}&periodType=${periodType || ''}&dateInSeconds=${dateInSeconds || '0'}`, {
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
  return fetch(`/kudos/rest/kudos/byActivity/${activityId}/all`, {
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
    return fetch(`/kudos/rest/kudos/byEntity?entityId=${entityId}&entityType=${entityType}&limit=${limit || 0}`, {
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
  return fetch(`/kudos/rest/kudos/byEntity/sent/count?entityId=${entityId}&entityType=${entityType}`, {
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
  return fetch(`/kudos/rest/kudos?dateInSeconds=${date}&limit=${limit || 0}`, {
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
  return fetch(`/kudos/rest/kudos/byDates?startDateInSeconds=${startDate}&endDateInSeconds=${endDate}&limit=${limit || 0}`, {
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
  return fetch(`/kudos/rest/kudos/period?dateInSeconds=${date}&periodType=${periodType}`, {
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
  extensionRegistry.registerExtension('profile-extension', 'action', {
    id: 'profile-kudos',
    title: title,
    icon: 'fa fa-award uiIconKudos uiIconLightBlue',
    class: 'fas fa-award',
    additionalClass: 'mt-1',
    order: 20,
    enabled: (profile) => profile.enabled && !profile.deleted && profile.username !== eXo.env.portal.userName,
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
  });
  extensionRegistry.registerExtension('profile-extension', 'action', {
    id: 'space-kudos',
    title: title,
    icon: 'fa-award',
    order: 10,
    iconOnly: true,
    enabled: space => !space.username && space.canRedactOnSpace,
    click: space => {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: space.id,
        type: 'SPACE_PROFILE',
        parentId: '',
        readOnlySpace: true,
        owner: eXo.env.portal.userName,
        spacePrettyName: space.prettyName,
        spaceId: space.id,
      }}));
    },
  });
}

export function registerOverviewExtension() {
  extensionRegistry.registerComponent('my-reputation-overview-kudos', 'my-reputation-item', {
    id: 'kudos-reputation-overview',
    vueComponent: Vue.options.components['kudos-overview-row'],
    rank: 10,
  }); 
}

export function registerComposerExtension() {
  extensionRegistry.registerComponent('ComposerAction', 'composer-action-item', {
    id: 'sendKudosButton',
    vueComponent: Vue.options.components['send-kudos-composer'],
    rank: 1,
  });
  extensionRegistry.registerComponent('ActivityToolbarAction', 'activity-toolbar-action', {
    id: 'sendKudosToolbarButton',
    vueComponent: Vue.options.components['send-kudos-toolbar-action'],
    rank: 10,
  });
}

export function registerFavoriteExtensions(title) {
  extensionRegistry.registerExtension('ActivityFavoriteIcon', 'activity-favorite-icon-extensions', {
    id: 'favorite-kudos',
    type: 'exokudos:activity',
    icon: 'fas fa-award',
    class: 'primary--text',
    title: activity => {  
      return `Kudos ${title} ${activity.kudosList[0].receiverFullName}`;
    }
  });
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
    isEnabled: (params) => params.activity
          && (!params.activityTypeExtension
          || !params.activityTypeExtension.canComment
          || params.activityTypeExtension.canComment(params.activity)),
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

  extensionRegistry.registerComponent('SpacePopover', 'space-popover-action', {
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
          const receiverIdentity = kudos.receiverType === 'user' && {
            id: kudos.receiverIdentityId,
            username: kudos.receiverId,
            fullName: kudos.receiverFullName,
            avatar: kudos.receiverAvatar,
            position: kudos.receiverPosition,
            external: String(kudos.externalReceiver),
            enabled: String(kudos.enabledReceiver),
            identityType: kudos.receiverType,
          } || {
            id: kudos.receiverIdentityId,
            prettyName: kudos.receiverId,
            displayName: kudos.receiverFullName,
            avatarUrl: kudos.receiverAvatar,
            external: String(kudos.externalReceiver),
            enabled: String(kudos.enabledReceiver),
            identityType: kudos.receiverType,
          };
          const url = kudos.receiverType === 'user' ? `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${kudos.receiverId}` : `${eXo.env.portal.context}/s/${kudos.receiverIdentityId}`;
          return {
            key: 'NewKudosSentActivityComment.activity_kudos_title',
            params: {
              0: `<a class="primary--text" href="${url}" v-identity-popover="${JSON.stringify(receiverIdentity).replace(/'/g, '\\\'').replace(/"/g, '\'')}">${kudos.receiverFullName}</a>`
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
        const summary = activityOrComment?.templateParams?.kudosMessage
                        || activityOrComment?.kudos?.message
                        || '';
        return summary.includes('<oembed>') && summary.split('<oembed>')[0] || summary;
      },
      regularFontSizeOnSummary: true,
      noTitleEllipsis: true,
      noSummaryEllipsis: true,
      supportsIcon: true,
      useSameViewForMobile: true,
      noEmbeddedLinkView: true,
      isCollapsed: true,
      summaryLinesToDisplay: 4,
      showEmbeddedPreview: true,
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
      forceCanEditOverwrite: true
    },
  });

  extensionRegistry.registerExtension('activity', 'expand-action-type', {
    id: 'KudosActivityReceiverNotification',
    rank: 30,
  });

  extensionRegistry.registerExtension('activity', 'action', {
    id: 'cancelKudos',
    labelKey: 'kudos.label.cancelKudos',
    icon: 'fa-undo-alt',
    confirmDialog: true,
    confirmMessageKey: 'kudos.label.confirmCancelKudos',
    confirmTitleKey: 'kudos.label.button.Confirmation',
    confirmOkKey: 'kudos.label.button.ok',
    confirmCancelKey: 'kudos.label.button.cancel',
    rank: 50,
    isEnabled: (activity, activityTypeExtension) => {
      return activity.type === 'exokudos:activity' && activity.canEdit === 'true' && (!activityTypeExtension.canEdit || activityTypeExtension.canEdit(activity));
    },
    click: (activity) => {
      const activityId = Number(activity.id);
      const kudos = activity.kudosList.find(kudosTmp => kudosTmp.activityId === activityId);
      document.dispatchEvent(new CustomEvent('kudos-cancel-action', {detail: kudos.technicalId}));
    },
  });

  extensionRegistry.registerExtension('activity', 'comment-action', {
    id: 'cancelKudos',
    rank: 30,
    labelKey: 'kudos.label.cancelKudos',
    icon: 'fa-undo-alt',
    confirmDialog: true,
    confirmMessageKey: 'kudos.label.confirmCancelKudos',
    confirmTitleKey: 'kudos.label.button.Confirmation',
    confirmOkKey: 'kudos.label.button.ok',
    confirmCancelKey: 'kudos.label.button.cancel',
    isEnabled: (activity, comment, activityTypeExtension) => {
      if (activityTypeExtension.canEdit) {
        if (activityTypeExtension.forceCanEditOverwrite) {
          return activityTypeExtension.canEdit(comment);
        } else if (!activityTypeExtension.canEdit(comment)) {
          return false;
        }
      }
      return comment.type === 'exokudos:activity' && comment.canEdit === 'true';
    },
    click: (activity, comment) => {
      document.dispatchEvent(new CustomEvent('kudos-cancel-action', {detail: comment.kudos.technicalId}));
    },
  });

  extensionRegistry.registerExtension('AnalyticsSamples', 'SampleItem', {
    type: 'kudos',
    options: {
      // Rank of executing 'match' method
      rank: 20,
      // Used Vue component to display value
      vueComponent: Vue.options.components['analytics-profile-sample-item-attribute'],
      match: fieldName => (fieldName === 'senderId' || fieldName === 'receiverId'),
      options: {
        isUser: true,
      },
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
