<template>
  <v-app v-if="!disabled" id="KudosActivityApp" color="transaprent" flat>
    <kudos-api />
    <v-dialog v-model="dialog" content-class="uiPopup with-overflow" width="500px" max-width="100vw" persistent @keydown.esc="dialog = false">
      <v-card class="elevation-12">
        <div class="popupHeader ClearFix">
          <a class="uiIconClose pull-right" aria-hidden="true" @click="dialog = false"></a>
          <span class="PopupTitle popupTitle">Send a kudos</span>
        </div>
        <v-card flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-card-text>
            <v-container flat fluid grid-list-lg class="pl-0 pr-0 pb-0 pt-0">
              <v-layout
                row
                wrap
                class="kudosIconsContainer">
                <v-card
                  v-for="(kudos, index) in allKudos"
                  :key="index"
                  flat
                  class="text-xs-center"
                  width="150px"
                  max-width="100%"
                  height="100px">
                  <v-card-text class="pb-0">
                    <v-icon :class="kudos.receiverFullName? 'uiIconBlue' : 'uiIconLightGray'" size="64" class="uiIconKudos">fa-award</v-icon>
                  </v-card-text>
                  <v-card-text class="pt-0">
                    <a v-if="kudos.receiverFullName" :href="kudos.receiverURL" rel="nofollow" target="_blank">{{ kudos.receiverFullName }}</a>
                  </v-card-text>
                </v-card>
              </v-layout>
            </v-container>
            <div v-if="remainingKudos <= 0" class="alert alert-info mt-5">
              <i class="uiIconInfo"></i>
              No kudos left. You 'll get more kudos to send in {{ remainingDaysToReset }} days.
            </div>
            <v-textarea
              v-else
              id="kudosMessage"
              v-model="kudosMessage"
              :disabled="loading"
              name="kudosMessage"
              label="Message"
              placeholder="Enter a message to send with your kudos"
              class="mt-4 mb-0"
              rows="3"
              flat
              no-resize />
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <button v-if="remainingKudos > 0" :disabled="loading || error" class="btn btn-primary mr-3" @click="send">Send</button>
            <button :disabled="loading" class="btn" @click="dialog = false">Close</button>
            <v-spacer />
          </v-card-actions>
        </v-card>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script>
import KudosApi from './KudosAPI.vue';

import {getReceiver} from '../js/KudosIdentity.js';
import {getEntityKudos, sendKudos, getKudos, getKudosByMonth} from '../js/Kudos.js';
import {initSettings} from '../js/KudosSettings.js';

export default {
  components: {
    KudosApi
  },
  data() {
    return {
      dialog: false,
      disabled: false,
      remainingKudos: 0,
      remainingDaysToReset: 0,
      entityIds: [],
      entityId: null,
      entityType: null,
      receiverType: null,
      receiverId: null,
      error: null,
      allKudosSent: [],
      allKudos: [],
      kudosMessage: null,
      loading: false,
      htmlToAppend: `<li class="SendKudosButtonTemplate">
          <button rel="tooltip" data-placement="bottom" title="Send Kudos" type="button" class="v-btn v-btn--icon small mt-0 mb-0 mr-0 ml-0" onclick="document.dispatchEvent(new CustomEvent('exo-kudo-open-send-modal', {'detail' : {'id' : 'entityId', 'type': 'entityType'}}))">
            <div class="v-btn__content">
              <i aria-hidden="true" class="fa fa-award uiIconKudos uiIconLightGray"></i>
            </div>
          </button>
          <a href="javascript:void(0);">kudosCount</a>
        </li>`
    };
  },
  watch: {
    entityId() {
      if(this.entityId && this.entityType) {
        this.allKudos = this.allKudosSent.slice(0);
        if (this.remainingKudos > 0) {
          getReceiver(this.entityType, this.entityId)
            .then(receiverDetails => {
              if (receiverDetails && receiverDetails.id && receiverDetails.type) {
                receiverDetails.isUserType = receiverDetails.type === 'organization' || receiverDetails.type === 'user';
                if(!receiverDetails.isUserType || receiverDetails.id !== eXo.env.portal.userName) {
                  this.receiverId = receiverDetails.id;
                  this.receiverType = receiverDetails.type;
  
                  this.allKudos.push({
                    receiverId: receiverDetails.id,
                    receiverType: receiverDetails.type,
                    receiverURL: receiverDetails.isUserType ? `/portal/intranet/profile/${receiverDetails.id}` : `/portal/g/:spaces:${receiverDetails.id}`,
                    receiverFullName: receiverDetails.fullname
                  });
                  for(let i = 0; i < (this.remainingKudos - 1); i++) {
                    this.allKudos.push({});
                  }
                } else {
                  throw new Error("You can't send kudos to yourself !");
                }
              } else {
                throw new Error("Receiver not found for entity type/id", this.entityType, this.entityId, receiverDetails);
              }
            })
            .catch(e => {
              this.error = String(e);
              console.debug("Error retrieving entity details with type and id", this.entityType, this.entityId, e);
            });
        }
      }
    }
  },
  created() {
    this.init()
      .then(() => {
        if (this.disabled) {
          return;
        }
        document.addEventListener('exo-kudo-open-send-modal', this.openDialog);

        // Attach link to activities
        $(window.parentToWatch).bind("DOMSubtreeModified", event => {
          this.addButtonToActivities();
        });
        this.$nextTick(() => {
          this.addButtonToActivities();
        });
      });
  },
  methods: {
    resetForm() {
      this.entityId = null;
      this.entityType = null;
      this.kudosMessage = null;
      this.error = null;
    },
    init() {
      return initSettings()
        .then(settings => {
          this.disabled = window.kudosSettings && window.kudosSettings.disabled;
          this.remainingKudos = Number(window.kudosSettings && window.kudosSettings.remainingKudos);
        })
        .then(settings => {
          this.remainingDaysToReset = this.getRemainingDays();
          // Get Kudos in an async way
          getKudos(eXo.env.portal.userName)
            .then(allKudos => {
              this.allKudosSent = allKudos ? allKudos : [];
            });
        })
        .catch(e => {
          this.error = e;
        });
    },
    addButtonToActivities() {
      if (!this.disabled) {
        const activitiesToAddButtons = $(window.parentToWatch).find('.activityStream .statusAction.pull-right:not(.kudoContainer)');
        activitiesToAddButtons.each((index, element) => {
          let activityId = $(element).closest('.activityStream').attr('id');
          if (activityId && this.entityIds.indexOf(activityId) < 0) {
            $(element).addClass('kudoContainer');
            const entityId = activityId;
            this.entityIds.push(entityId);
            activityId = activityId ? activityId.replace('activityContainer', '') : null;
            this.refreshLink(element, 'ACTIVITY', activityId)
              .then(() => {
                const index = this.entityIds.indexOf(entityId);
                if (index >= 0) {
                  this.entityIds.splice(index, 1);
                }
              });
          }
        });
      }
    },
    refreshLink(element, entityType, entityId) {
      return getEntityKudos(entityType, entityId)
        .then(kudosList => {
          const linkId = `SendKudosButtonACTIVITY${entityId}`;
          const hasSentKudos = kudosList && kudosList.find(kudos => kudos.senderId === eXo.env.portal.userName);
          const kudosCount = kudosList ? kudosList.length : 0;
          const $sendKudosLink = $(this.htmlToAppend.replace('entityId', entityId).replace('entityType', entityType).replace('kudosCount', kudosCount).replace('uiIconLightGray', hasSentKudos ? 'uiIconBlue' : 'uiIconLightGray'));
          $sendKudosLink.attr('id', linkId);
          $sendKudosLink.data("kudosList", kudosList);
          const $existingLink = $(`#${linkId}`);
          if ($existingLink.length) {
            $existingLink.html($sendKudosLink.html());
          } else if(element) {
            $sendKudosLink.prependTo($(element));
          } else {
            console.warn("Can't refresh entity with type/id", entityType, entityId);
          }
        });
    },
    openDialog(event) {
      if (!this.disabled) {
        this.dialog = true;
        this.resetForm();
        this.entityType = event && event.detail && event.detail.type;
        this.entityId = event && event.detail && event.detail.id;
      }
    },
    send() {
      this.loading = true;
      this.error = null;
      sendKudos({
        entityType: this.entityType,
        entityId: this.entityId,
        receiverType: this.receiverType,
        receiverId: this.receiverId,
        message: this.kudosMessage
      })
        .then(status => {
          if(!status) {
            throw new Error("Error sending Kudo, please contact your administrator.");
          }
        })
        .catch(e => {
          console.debug("Error saving kudo", e);
          this.error = String(e);
          throw e;
        })
        .then(() =>
          this.init()
            .catch(e => {
              console.debug("Error refreshing allowed number of kudos for current user", e);
            })
        )
        .then(() => 
          this.refreshLink(null, this.entityType, this.entityId)
            .catch(e => {
              console.debug("Error refreshing number of kudos", e);
            })
        )
        .then(() => this.dialog = false)
        .catch(e => {
          console.debug("Error saving kudo");
          this.error = String(e);
        })
        .finally(() => {
          this.loading = false;
          if(!this.dialog) {
            if(this.entityType === 'ACTIVITY') {
              this.refreshActivity(this.entityId);
            }
            this.resetForm();
          }
        });
    },
    getRemainingDays() {
      const now = new Date();
      const endOfMonth = new Date(now.getTime());
      endOfMonth.setMonth(now.getMonth() + 1);
      endOfMonth.setDate(0);
      return endOfMonth.getDate() > now.getDate() ? endOfMonth.getDate() - now.getDate() : 0;
    },
    refreshActivity(activityId) {
      const $activityItem = $(`#UIActivityLoader${activityId}`);
      $activityItem.data('url', $('.uiActivitiesLoaderURL').data('url'));
      $activityItem.addClass("activity-loadding");
      UIActivityLoader.renderActivity($activityItem);
    }
  }
};
</script>