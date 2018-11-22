<template>
  <v-app v-if="!disabled" id="KudosActivityApp" color="transaprent" flat>
    <v-dialog v-model="dialog" content-class="uiPopup with-overflow" width="500px" max-width="100vw" persistent @keydown.esc="dialog = false">
      <v-card class="elevation-12">
        <div class="popupHeader ClearFix">
          <a class="uiIconClose pull-right" aria-hidden="true" @click="dialog = false"></a>
          <span class="PopupTitle popupTitle">Send Kudos</span>
        </div>
        <v-card flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>{{ error }}
          </div>
          <v-card-text>
            <auto-complete
              ref="receiverAutoComplete"
              input-label="Kudos receiver"
              input-placeholder="Select a user or a space"
              disabled
              @item-selected="receiverType = $event.type;receiverId = $event.id;"/>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <button :disabled="loading" class="btn btn-primary mr-3" @click="send">Send</button>
            <button :disabled="loading" class="btn" @click="dialog = false">Close</button>
            <v-spacer />
          </v-card-actions>
        </v-card>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script>
import {getReceiver, getEntityKudos, sendKudos} from '../js/KudosIdentity.js';
import {initSettings} from '../js/KudosSettings.js';

import AutoComplete from './AutoComplete.vue';

export default {
  components: {
    AutoComplete
  },
  data() {
    return {
      dialog: false,
      disabled: false,
      remainingKudos: null,
      entityIds: [],
      entityId: null,
      entityType: null,
      receiverType: null,
      receiverId: null,
      error: null,
      loading: false,
      htmlToAppend: `<li class="SendKudosButtonTemplate">
          <button type="button" class="v-btn v-btn--icon small mt-0 mb-0 mr-0 ml-0" onclick="document.dispatchEvent(new CustomEvent('exo-kudo-open-send-modal', {'detail' : {'id' : 'entityId', 'type': 'entityType'}}))">
            <div class="v-btn__content">
              <i aria-hidden="true" class="material-icons uiIconFavorite uiIconLightGray">favorite</i>
            </div>
          </button>
          <a href="javascript:void(0);">(kudosCount)</a>
        </li>`
    };
  },
  watch: {
    dialog() {
      if(!this.dialog) {
        this.entityId = null;
        this.entityType = null;
      }
    },
    entityId() {
      if(this.entityId && this.entityType) {
        getReceiver(this.entityType, this.entityId)
          .then(receiverDetails => {
            if (receiverDetails && receiverDetails.id && receiverDetails.type) {
              if((receiverDetails.type !== 'organization' && receiverDetails.type !== 'user') || receiverDetails.id !== eXo.env.portal.userName) {
                this.$refs.receiverAutoComplete.selectItem(receiverDetails.id, receiverDetails.type);
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
    init() {
      return initSettings()
        .then(settings => {
          this.disabled = window.kudosSettings && window.kudosSettings.disabled;
          this.remainingKudos = Number(window.kudosSettings && window.kudosSettings.remainingKudos);
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
          const kudosCount = kudosList ? kudosList.reduce((a, b) => a + b.num, 0) : 0;
          const $sendKudosLink = $(this.htmlToAppend.replace('entityId', entityId).replace('entityType', entityType).replace('kudosCount', kudosCount).replace('uiIconLightGray', hasSentKudos ? 'uiIconBlue' : 'uiIconLightGray'));
          $sendKudosLink.attr('id', linkId);
          if (this.remainingKudos <= 0) {
            $sendKudosLink.find('button').attr("disabled", "disabled");
          }
          $sendKudosLink.data("kudosList", kudosList);
          const $existingLink = $(`#${linkId}`);
          if ($existingLink.length) {
            $existingLink.html($sendKudosLink.html());
          } else if(element) {
            $sendKudosLink.appendTo($(element));
          } else {
            console.warn("Can't refresh entity with type/id", entityType, entityId);
          }
        });
    },
    openDialog(event) {
      if (!this.disabled) {
        this.dialog = true;
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
        num: 1
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
        .finally(() => this.loading = false);
    }
  }
};
</script>