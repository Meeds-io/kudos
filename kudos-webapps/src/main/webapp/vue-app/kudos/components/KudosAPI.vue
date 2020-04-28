<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<script>
import {getKudosByPeriod, getKudosByPeriodOfDate, getPeriodDates, registerExternalExtensions} from '../../js/Kudos.js';

export default {
  created() {
    document.addEventListener('exo-kudos-get-period', this.getPeriodDates);
    document.addEventListener('exo-kudos-get-kudos-list', this.getKudosList);
    registerExternalExtensions(this.$t('exoplatform.kudos.title.sendAKudos'));
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
        return getKudosByPeriodOfDate(detail.date);
      } else {
        return getKudosByPeriod(detail.startDate, detail.endDate);
      }
    },
    initTiptip() {
      window.eXo = window.eXo ? window.eXo : {};
      eXo.social = eXo.social ? eXo.social : {};
      eXo.social.tiptip = eXo.social.tiptip ? eXo.social.tiptip : {};
      eXo.social.tiptip.extraActions = eXo.social.tiptip.extraActions ? eXo.social.tiptip.extraActions : [];
      const sendKudosLabel = this.$t('exoplatform.kudos.button.sendKudos');
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
            divUIAction.append(`<a title="${sendKudosLabel}" 
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
                <span> ${sendKudosLabel}</span>
              </a>
            </li>`);
        }
      }
    }
  }
};
</script>