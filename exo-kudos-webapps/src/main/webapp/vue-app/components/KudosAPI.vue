<template>
  <span></span>
</template>

<script>
import {getAllKudosByPeriod, getAllKudosByPeriodOfDate, getPeriodDates} from '../js/Kudos.js';

export default {
  created() {
    document.addEventListener('exo-kudos-get-period', this.getPeriodDates);
    document.addEventListener('exo-kudos-get-kudos-list', this.getKudosList);
  },
  methods: {
    init() {
      this.initTiptip();
    },
    getPeriodDates(event) {
      if(event && event.detail && event.detail.date && event.detail.periodType) {
        getPeriodDates(event.detail.date, event.detail.periodType)
          .then(period =>
            document.dispatchEvent(new CustomEvent('exo-kudos-get-period-result', {'detail' : {'period' : period}}))
          );
      }
    },
    getKudosList(event) {
      if(event && event.detail && (event.detail.date || (event.detail.startDate && event.detail.endDate))) {
        document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list-loading'));
        const kudosIdentitiesMap = {};
        this.getKudosByDate(event.detail)
          .then(kudosListByPeriod => {
            kudosListByPeriod.forEach(kudos => {
              if (kudosIdentitiesMap[kudos.senderId]) {
                kudosIdentitiesMap[kudos.senderId].sent++;
              } else {
                kudosIdentitiesMap[kudos.senderId] = {
                  name: kudos.senderFullName,
                  identityId: kudos.senderIdentityId,
                  id: kudos.senderId,
                  url: kudos.senderURL,
                  type: 'user',
                  avatar: kudos.senderAvatar,
                  sent: 1,
                  received: 0
                };
              }
              if (kudosIdentitiesMap[kudos.receiverId]) {
                kudosIdentitiesMap[kudos.receiverId].received++;
              } else {
                kudosIdentitiesMap[kudos.receiverId] = {
                  name: kudos.receiverFullName,
                  identityId: kudos.receiverIdentityId,
                  id: kudos.receiverId,
                  url: kudos.receiverURL,
                  type: kudos.receiverType,
                  avatar: kudos.receiverAvatar,
                  sent: 0,
                  received: 1
                };
              }
            });
            document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list-result', {'detail' : {'list' : Object.values(kudosIdentitiesMap)}}));
          })
          .catch(e => {
            document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list-result', {'detail' : {'error' : e}}));
          });
      } else {
        console.debug('Event seems to be empty, please verify the API usage');
      }
    },
    getKudosByDate(detail) {
      if(detail.date) {
        return getAllKudosByPeriodOfDate(detail.date);
      } else {
        return getAllKudosByPeriod(detail.startDate, detail.endDate);
      }
    },
    initTiptip() {
      window.eXo = window.eXo ? window.eXo : {};
      eXo.social = eXo.social ? eXo.social : {};
      eXo.social.tiptip = eXo.social.tiptip ? eXo.social.tiptip : {};
      eXo.social.tiptip.extraActions = eXo.social.tiptip.extraActions ? eXo.social.tiptip.extraActions : [];
      eXo.social.tiptip.extraActions.push({
        appendContentTo(divUIAction, ownerId, type) {
          if(!type || type === 'username' || type === 'user' || type === 'organization') {
            type = 'USER';
          } else {
            type = 'SPACE';
          }
          // FIXME disable TIPTIP button to send Kudos to a space because of a limitation
          // in eXo Platform REST Services that couldn't retrieve Space details by prettyName
          if(type === 'USER') {
            divUIAction.append(`<a title="Send Kudos" 
                class="btn sendKudosTipTipButton"
                href="javascript:void(0);"
                onclick="document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal',
                    {'detail' : {'id' : '${ownerId}', 'type': '${type}_TIPTIP', ignoreRefresh: true}}))">
                  <i class="uiIcon fa fa-award uiIconKudosTipTip"></i>
              </a>`);
          }
        }
      });
      if(!$(".SendKudosButtonBanner").length) {
        if ($('.profileMenuNav .profileMenuNavHeader .profileMenuApps').length && eXo && eXo.env && eXo.env.portal && eXo.env.portal.profileOwner && eXo.env.portal.profileOwner !== eXo.env.portal.userName) {
          $('.profileMenuNav .profileMenuNavHeader .profileMenuApps').append(`<li class="SendKudosButtonBanner">
              <a onclick="document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal',
               {'detail' : {'id' : '${eXo.env.portal.profileOwner}', 'type': 'USER_PROFILE', ignoreRefresh: true}}));"
               class="btn" href="javascript:void(0);">
                <i class="uiIcon fa fa-award uiIconKudos"></i>
                <span> Send Kudos</span>
              </a>
            </li>`);
        }
      }
    }
  }
};
</script>