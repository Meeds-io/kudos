<template>
  <v-app v-if="!disabled" id="KudosApp" color="transaprent" flat>
    <kudos-api ref="kudosAPI" />
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
          <v-card-text v-if="allKudos && allKudos.length">
            <v-container flat fluid grid-list-lg class="pl-0 pr-0 pb-0 pt-0">
              <v-layout
                row
                wrap
                class="kudosIconsContainer">
                <v-card
                  v-for="(kudos, index) in allKudos"
                  :key="index"
                  :class="kudos.isCurrent && 'kudosIconContainerCurrent'"
                  flat
                  class="text-xs-center kudosIconContainerTop">
                  <v-card-text v-if="kudos.receiverFullName && !kudos.isCurrent" class="kudosIconContainer">
                    <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                    <v-icon class="uiIconKudosCheck uiIconBlue" size="16">fa-check-circle</v-icon>
                  </v-card-text>
                  <v-card-text v-else-if="kudos.isCurrent" class="kudosIconContainer">
                    <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                  </v-card-text>
                  <v-card-text v-else class="kudosIconContainer">
                    <v-icon :title="`${remainingKudos -1} kudos left to send`" class="uiIconKudos uiIconLightGrey" size="64">fa-award</v-icon>
                  </v-card-text>
                  <div v-if="kudos.isCurrent" class="kudosIconContainerCurrent"></div>
                  <!-- Made absolute because when isCurrent = true, the item 'kudosIconContainerCurrent' will hide this block, thus no tiptip and no link click is possible -->
                  <v-card-text v-if="kudos.receiverFullName" class="kudosIconLink absoluteLink">
                    <identity-link
                      :technical-id="kudos.receiverIdentityId"
                      :id="kudos.receiverId"
                      :type="kudos.receiverType"
                      :name="kudos.receiverFullName" />
                  </v-card-text>
                  <!-- The same block is displayed again because the first block is absolute, so this is to ensure that the element is displayed in its correct position -->
                  <v-card-text v-if="kudos.receiverFullName" class="kudosIconLink kudosIconLinkInvisible">
                    <identity-link
                      :technical-id="kudos.receiverIdentityId"
                      :id="kudos.receiverId"
                      :type="kudos.receiverType"
                      :name="kudos.receiverFullName" />
                  </v-card-text>
                  <v-card-text v-else class="pb-0 pt-0">
                    <a :title="`${remainingKudos -1} kudos left to send`" href="javascript:void(0);">X {{ remainingKudos - 1 }}</a>
                  </v-card-text>
                </v-card>
              </v-layout>
            </v-container>
            <div v-if="remainingKudos <= 0" class="alert alert-info mt-5">
              <i class="uiIconInfo"></i>
              No kudos left. You 'll get more kudos to send in {{ remainingDaysToReset }} {{ remainingDaysToReset === 1 ? 'day' : 'days' }}.
            </div>
            <v-form v-else-if="kudosToSend" ref="form">
              <v-textarea
                id="kudosMessage"
                v-model="kudosMessage"
                :disabled="loading"
                :rules="kudosMessageRules"
                name="kudosMessage"
                label="Message"
                placeholder="Enter a message to send with your kudos"
                class="mt-4 mb-0"
                rows="3"
                flat
                no-resize />
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <button v-if="kudosToSend" :disabled="loading || error" class="btn btn-primary mr-3" @click="send">Send</button>
            <button :disabled="loading" class="btn" @click="dialog = false">Close</button>
            <v-spacer />
          </v-card-actions>
        </v-card>
      </v-card>
    </v-dialog>

    <v-dialog v-model="listDialog" content-class="uiPopup with-overflow" width="500px" max-width="100vw" persistent @keydown.esc="listDialog = false">
      <v-card class="elevation-12">
        <div class="popupHeader ClearFix">
          <a class="uiIconClose pull-right" aria-hidden="true" @click="listDialog = false"></a>
          <span class="PopupTitle popupTitle">Kudos list</span>
        </div>
        <v-card flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-card-text>
            <v-container v-if="kudosList && kudosList.length" flat fluid grid-list-lg class="pl-0 pr-0 pb-0 pt-0">
              <v-layout
                row
                wrap
                class="kudosIconsContainer">
                <v-card
                  v-for="(kudos, index) in kudosList"
                  :key="index"
                  :class="kudos.isCurrent && 'kudosIconContainerCurrent'"
                  flat
                  class="text-xs-center kudosIconContainerTop">
                  <v-card-text v-if="kudos.senderFullName" class="kudosIconContainer">
                    <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                    <v-icon class="uiIconKudosCheck uiIconBlue" size="16">fa-check-circle</v-icon>
                  </v-card-text>
                  <v-card-text v-if="kudos.senderFullName" class="kudosIconLink">
                    <identity-link
                      :technical-id="kudos.senderIdentityId"
                      :id="kudos.senderId"
                      :type="kudos.senderType"
                      :name="kudos.senderFullName" />
                  </v-card-text>
                </v-card>
              </v-layout>
            </v-container>
            <div v-else class="alert alert-info">
              <i class="uiIconInfo"></i>
              This activity haven't received a kudos yet.
            </div>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <button class="btn" @click="listDialog = false">Close</button>
            <v-spacer />
          </v-card-actions>
        </v-card>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script>
import KudosApi from './KudosAPI.vue';
import IdentityLink from './IdentityLink.vue';

import {getReceiver} from '../js/KudosIdentity.js';
import {getEntityKudos, sendKudos, getKudos} from '../js/Kudos.js';
import {initSettings} from '../js/KudosSettings.js';

export default {
  components: {
    KudosApi,
    IdentityLink
  },
  data() {
    return {
      dialog: false,
      listDialog: false,
      ignoreRefresh: false,
      kudosList: false,
      disabled: false,
      remainingKudos: 0,
      remainingDaysToReset: 0,
      entityIds: [],
      parentEntityId: null,
      entityId: null,
      entityType: null,
      receiverType: null,
      receiverId: null,
      error: null,
      allKudosSent: [],
      allKudos: [],
      kudosToSend: null,
      kudosMessage: null,
      loading: false,
      kudosMessageRules: [
        (v) => !!v || 'Required field',
        (v) => (v && this.escapeCharacters(v).replace(/ /g, '').length > 9) || 'A least 10 real characters',
        (v) => (v && this.escapeCharacters(v).split(' ').length > 2) || 'Min 3 words',
      ],
      htmlToAppend: `<li class="SendKudosButtonTemplate">
          <button rel="tooltip" data-placement="bottom" title="Send Kudos" type="button" class="v-btn v-btn--icon small mt-0 mb-0 mr-0 ml-0" onclick="document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {'detail' : {'id' : 'entityId', 'type': 'entityType', 'parentId': 'parentEntityId'}}));event.preventDefault();event.stopPropagation();">
            <div class="v-btn__content">
              <i aria-hidden="true" class="fa fa-award uiIconKudos uiIconLightGrey"></i>
            </div>
          </button>
          <a rel="tooltip" data-placement="top" title="Click to display kudos" href="javascript:void(0);" class="grey--text" onclick="document.dispatchEvent(new CustomEvent('exo-kudos-open-kudos-list', {'detail' : {'id' : 'entityId', 'type': 'entityType'}}));event.preventDefault();event.stopPropagation();"> (kudosCount) </a>
        </li>`
    };
  },
  watch: {
    listDialog() {
      if(!this.listDialog || !this.entityId || !this.entityType) {
        return;
      }
      const $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${this.entityType}${this.entityId}`);
      const kudosList = $sendKudosLink.data("kudosList");
      if(!kudosList || !kudosList.length) {
        return;
      }
      this.kudosList = kudosList;
    },
    dialog() {
      if(!this.dialog) {
        return;
      }
      this.kudosMessage = null;
      this.kudosToSend = null;
      this.error = null;
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
                  const kudosToSend = {
                    receiverId: receiverDetails.id,
                    receiverType: receiverDetails.type,
                    receiverIdentityId: receiverDetails.identityId,
                    receiverURL: receiverDetails.isUserType ? `/portal/intranet/profile/${receiverDetails.id}` : `/portal/g/:spaces:${receiverDetails.id}`,
                    receiverFullName: receiverDetails.fullname,
                    isCurrent: true
                  };
                  if(receiverDetails.entityId) {
                    this.entityId = receiverDetails.entityId;
                  }
                  if(receiverDetails.notAuthorized) {
                    this.error = "Current selected user isn't authorized to receive Kudos.";
                  } else {
                    this.kudosToSend = kudosToSend;
                  }
                  this.allKudos.push(kudosToSend);
                  if (this.remainingKudos > 1) {
                    this.allKudos.push({});
                  }
                  this.$nextTick(() => {
                    if($(".kudosIconContainerTop.kudosIconContainerCurrent").length) {
                      $(".kudosIconContainerTop.kudosIconContainerCurrent")[0].scrollIntoView();
                    }
                  });
                } else {
                  throw new Error("You can't send kudos to yourself !");
                }
              } else {
                console.debug("Receiver not found for entity type/id", this.entityType, this.entityId, receiverDetails);
                throw new Error("The receiver isn't authorized to receive kudos");
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
        this.$refs.kudosAPI.init();

        document.addEventListener('exo-kudos-open-send-modal', this.openDialog);
        document.addEventListener('exo-kudos-open-kudos-list', this.openListDialog);

        // Attach link to activities
        $(window.parentToWatch).bind("DOMSubtreeModified", event => {
          this.addButtonToActivities();
          this.addButtonToComments();
        });
        this.$nextTick(() => {
          this.addButtonToActivities();
          this.addButtonToComments();
        });
      });
  },
  methods: {
    init() {
      return initSettings()
        .then(() => {
          this.disabled = window.kudosSettings && window.kudosSettings.disabled;
          this.remainingKudos = Number(window.kudosSettings && window.kudosSettings.remainingKudos);
        })
        .then(() => {
          const remainingDaysToReset = Number(this.getRemainingDays());
          this.remainingDaysToReset = remainingDaysToReset ? remainingDaysToReset : 0;
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
    addButtonToComments() {
      if (!this.disabled) {
        const commentsToAddButtons = $(window.parentToWatch).find('.activityStream .commentItem .statusAction:not(.kudoContainer)');
        commentsToAddButtons.each((index, element) => {
          let commentId = $(element).closest('.CommentBlock').data('comment-id');
          if (commentId && this.entityIds.indexOf(commentId) < 0) {
            $(element).addClass('kudoContainer');
            const entityId = commentId;
            this.entityIds.push(entityId);
            commentId = commentId.replace('comment', '');
            let activityId = $(element).closest('.activityStream').attr('id');
            activityId = activityId ? activityId.replace('activityContainer', '') : '';
            this.refreshLink(element, 'COMMENT', commentId, activityId)
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
    addButtonToActivities() {
      if (!this.disabled) {
        const activitiesToAddButtons = $(window.parentToWatch).find('.activityStream .statusAction.pull-right:not(.kudoContainer)');
        activitiesToAddButtons.each((index, element) => {
          let activityId = $(element).closest('.activityStream').attr('id');
          if (activityId && this.entityIds.indexOf(activityId) < 0) {
            $(element).addClass('kudoContainer');
            const entityId = activityId;
            this.entityIds.push(entityId);
            activityId = activityId.replace('activityContainer', '');
            this.refreshLink(element, 'ACTIVITY', activityId, '')
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
    refreshLink(element, entityType, entityId, parentEntityId) {
      if(this.ignoreRefresh) {
        return Promise.resolve(null);
      }
      return getEntityKudos(entityType, entityId)
        .then(kudosList => {
          const linkId = `SendKudosButton${entityType}${entityId}`;
          const hasSentKudos = kudosList && kudosList.find(kudos => kudos.senderId === eXo.env.portal.userName);
          const kudosCount = kudosList ? kudosList.length : 0;
          let $sendKudosLink = $(this.htmlToAppend.replace(new RegExp('entityId', 'g'), entityId).replace(new RegExp('entityType', 'g'), entityType).replace(new RegExp('parentEntityId', 'g'), parentEntityId ? parentEntityId : '').replace('kudosCount', kudosCount).replace('LightGrey', hasSentKudos ? 'Blue' : 'LightGrey').replace('grey', hasSentKudos ? 'primary' : 'grey'));
          $sendKudosLink.attr('id', linkId);
          const $existingLink = $(`#${linkId}`);
          if ($existingLink.length) {
            $existingLink.html($sendKudosLink.html());
          } else if(element) {
            if (entityType === 'COMMENT') {
              $('<li class="separator">-</li>').appendTo($(element));
              $sendKudosLink.appendTo($(element));
            } else {
              $sendKudosLink.prependTo($(element));
            }
          } else {
            console.warn("Can't refresh entity with type/id", entityType, entityId);
            return;
          }
          $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${entityType}${entityId}`);
          $sendKudosLink.data("kudosList", kudosList);
        });
    },
    openDialog(event) {
      if (!this.disabled) {
        this.error = null;
        this.entityType = event && event.detail && event.detail.type;
        this.entityId = event && event.detail && event.detail.id;
        this.parentEntityId = event && event.detail && event.detail.parentId;
        this.ignoreRefresh = event && event.detail && event.detail.ignoreRefresh;
        this.dialog = true;
      }
    },
    openListDialog(event) {
      if (!this.disabled) {
        this.error = null;
        this.kudosList = [];
        this.entityType = event && event.detail && event.detail.type;
        this.entityId = event && event.detail && event.detail.id;
        this.listDialog = true;
      }
    },
    send() {
      this.error = null;

      if(!this.$refs.form.validate()) {
        return;
      }

      this.loading = true;
      sendKudos({
        entityType: this.entityType,
        entityId: this.entityId,
        parentEntityId: this.parentEntityId,
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
        .then(() => {
          return this.init()
            .catch(e => {
              console.debug("Error refreshing allowed number of kudos for current user", e);
            });
        })
        .then(() => {
          return this.refreshLink(null, this.entityType, this.entityId, this.parentEntityId)
            .catch(e => {
              console.debug("Error refreshing number of kudos", e);
            });
        })
        .then(() => this.dialog = false)
        .catch(e => {
          console.debug("Error refreshing UI", e);
          this.error = String(e);
        })
        .finally(() => {
          this.loading = false;
          if(!this.dialog) {
            if(this.entityType === 'ACTIVITY') {
              this.refreshActivity(this.entityId);
            } else if(this.entityType === 'COMMENT') {
              let activityId = $(`#commentContainercomment${this.entityId}`).closest('.activityStream').attr('id');
              if (activityId) {
                const thiss = this;
                activityId = activityId.replace('activityContainer', '');
                thiss.refreshActivity(activityId);
                setTimeout(() => {
                  let commentToScrollTo = $(`[data-parent-comment=comment${this.entityId}]`).last().attr("id");
                  if(commentToScrollTo) {
                    commentToScrollTo = commentToScrollTo.replace('commentContainer', '');
                    UIActivity.focusToComment(commentToScrollTo);
                  }
                }, 400);
              }
            } else {
              if($(".activityStreamStatus .uiIconRefresh").length && $(".activityStreamStatus .uiIconRefresh").is(":visible")) {
                $(".activityStreamStatus .uiIconRefresh").click();
              }
            }
          }
        });
    },
    getRemainingDays() {
      const remainingDateInMillis = window.kudosSettings.endPeriodDateInSeconds * 1000 - Date.now();
      if(remainingDateInMillis < 0) {
        return 0;
      }
      return parseInt(remainingDateInMillis / 86400000) + 1;
    },
    refreshActivity(activityId) {
      const $activityItem = $(`#UIActivityLoader${activityId}`);
      $activityItem.data('url', $('.uiActivitiesLoaderURL').data('url'));
      $activityItem.addClass("activity-loadding");
      UIActivityLoader.renderActivity($activityItem);
    },
    escapeCharacters(value) {
      return value.replace(/((\r\n)|\n|\r)/g, '').replace(/(\.|,|\?|!)/g, ' ').replace(/( )+/g, ' ').trim();
    },
  }
};
</script>