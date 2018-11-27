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
    }
  }
};
</script>