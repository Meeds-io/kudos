<script>
export default {
  created() {
    document.addEventListener('exo-kudos-get-period', this.getPeriodDates);
    document.addEventListener('exo-kudos-get-kudos-list', this.getKudosList);
    this.$kudosService.registerExternalExtensions(this.$t('exoplatform.kudos.title.sendAKudos'));
    this.$kudosService.registerFavoriteExtensions(this.$t('exoplatform.kudos.label.to'));
    this.$kudosService.registerOverviewExtension();
    this.$kudosService.registerActivityActionExtension();
    this.$kudosService.registerActivityReactionTabs();
    document.addEventListener('display-activity-details', this.getActivityInformations);
  },
  methods: {
    init() {
      this.initTiptip();
    },
    getActivityInformations(event) {
      const entityType = event && event.detail && event.detail.type;
      const entityId = event && event.detail && event.detail.id;
      return this.$kudosService.getEntityKudos(entityType, entityId).then(kudosList => {
        const kudosCount = kudosList ? kudosList.length : 0;
        this.$kudosService.registerActivityReactionTabs(entityType, entityId, kudosCount, kudosList);
      });
    },
    getPeriodDates(event) {
      if (event && event.detail && event.detail.date && event.detail.periodType) {
        this.$kudosService.getPeriodDates(event.detail.date, event.detail.periodType)
          .then(period =>
            document.dispatchEvent(new CustomEvent('exo-kudos-get-period-result', {'detail': {'period': period}}))
          );
      }
    },
    getKudosList(event) {
      if (event && event.detail && (event.detail.date || (event.detail.startDate && event.detail.endDate))) {
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
            document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list-result', {'detail': {'list': Object.values(kudosIdentitiesMap)}}));
          })
          .catch(e => {
            document.dispatchEvent(new CustomEvent('exo-kudos-get-kudos-list-result', {'detail': {'error': e}}));
          });
      } else {
        console.error('Event seems to be empty, please verify the API usage');
      }
    },
    getKudosByDate(detail) {
      if (detail.date) {
        return this.$kudosService.getKudosByPeriodOfDate(detail.date);
      } else {
        return this.$kudosService.getKudosByPeriod(detail.startDate, detail.endDate);
      }
    },
    initTiptip() {
      window.eXo = window.eXo ? window.eXo : {};
      eXo.social = eXo.social ? eXo.social : {};
      eXo.social.tiptip = eXo.social.tiptip ? eXo.social.tiptip : {};
      eXo.social.tiptip.extraActions = eXo.social.tiptip.extraActions ? eXo.social.tiptip.extraActions : [];
      const sendKudosLabel = this.$t('exoplatform.kudos.button.sendKudos');
      if (eXo.social.tiptip.extraActions.find(action => action.id === 'profile-kudos')) {
        return;
      }
      eXo.social.tiptip.extraActions.push({
        id: 'profile-kudos',
        appendContentTo(divUIAction, ownerId, type) {
          if (!type || type === 'username' || type === 'user' || type === 'organization') {
            type = 'USER';
          } else {
            type = 'SPACE';
          }
          // FIXME disable TIPTIP button to send Kudos to a space because of a limitation
          // in eXo Platform REST Services that couldn't retrieve Space details by prettyName
          if (type === 'USER') {
            divUIAction.append(`<a title="${sendKudosLabel}"
                class="sendKudosTipTipButton"
                href="javascript:void(0);"
                onclick="document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal',
                    {'detail' : {'id' : '${ownerId}', 'type': '${type}_TIPTIP', ignoreRefresh: true}}))">
                  <i class="uiIcon fa fa-award uiIconKudosTipTip"></i>
              </a>`);
          }
        }
      });
      if (!$('.SendKudosButtonBanner').length) {
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