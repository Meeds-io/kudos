<template>
  <span></span>
</template>

<script>
import {getKudosByMonth} from '../js/Kudos.js';

export default {
  created() {
    document.addEventListener('exo-kudo-get-kudos-list', this.getKudosList);
  },
  methods: {
    getKudosList(event) {
      if(event && event.detail && event.detail.month) {
        document.dispatchEvent(new CustomEvent('exo-kudo-get-kudos-list-loading'));
        const kudosIdentitiesMap = {};
        getKudosByMonth(event.detail.month)
          .then(kudosListByMonth => {
            kudosListByMonth.forEach(kudos => {
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
            document.dispatchEvent(new CustomEvent('exo-kudo-get-kudos-list-result', {'detail' : {'list' : Object.values(kudosIdentitiesMap)}}));
          })
          .catch(e => {
            document.dispatchEvent(new CustomEvent('exo-kudo-get-kudos-list-result', {'detail' : {'error' : e}}));
          });
      } else {
        console.debug('Event seems to be empty, please verify the API usage');
      }
    }
  }
};
</script>