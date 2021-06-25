<template>
  <v-app
    v-if="!disabled"
    id="KudosApp"
    color="transaprent"
    class="VuetifyApp"
    flat>
    <kudos-api ref="kudosAPI" />
    <exo-modal
      ref="sendKudosModal"
      :title="$t('exoplatform.kudos.title.sendAKudos')"
      width="500px"
      hide-actions
      @dialog-opened="dialog = true"
      @dialog-closed="dialog = false">
      <v-card flat>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>{{ error }}
        </div>
        <v-card-text v-if="allKudos && allKudos.length && !error">
          <v-container
            flat
            fluid
            grid-list-lg
            class="pa-0">
            <v-layout
              row
              wrap
              class="kudosIconsContainer">
              <v-card
                v-for="(kudos, index) in allKudos"
                :key="index"
                :class="kudos.isCurrent && 'kudosIconContainerCurrent'"
                flat
                class="text-center kudosIconContainerTop">
                <v-card-text v-if="kudos.receiverFullName && !kudos.isCurrent" class="kudosIconContainer">
                  <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                  <v-icon class="uiIconKudosCheck uiIconBlue" size="16">fa-check-circle</v-icon>
                </v-card-text>
                <v-card-text v-else-if="kudos.isCurrent" class="kudosIconContainer">
                  <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                </v-card-text>
                <v-card-text v-else class="kudosIconContainer">
                  <v-icon
                    :title="$t('exoplatform.kudos.label.remainingKudos', (remainingKudos -1))"
                    class="uiIconKudos uiIconLightGrey"
                    size="64">
                    fa-award
                  </v-icon>
                </v-card-text>
                <div v-if="kudos.isCurrent" class="kudosIconContainerCurrent"></div>
                <!-- Made absolute because when isCurrent = true, the item 'kudosIconContainerCurrent' will hide this block, thus no tiptip and no link click is possible -->
                <v-card-text v-if="kudos.receiverFullName" class="kudosIconLink">
                  <kudos-identity-link
                    :id="kudos.receiverId"
                    :technical-id="kudos.receiverIdentityId"
                    :type="kudos.receiverType"
                    :name="kudos.receiverFullName" />
                </v-card-text>
                <!-- The same block is displayed again because the first block is absolute, so this is to ensure that the element is displayed in its correct position -->
                <v-card-text v-else-if="kudos.receiverFullName" class="kudosIconLink kudosIconLinkInvisible">
                  <kudos-identity-link
                    :id="kudos.receiverId"
                    :technical-id="kudos.receiverIdentityId"
                    :type="kudos.receiverType"
                    :name="kudos.receiverFullName" />
                </v-card-text>
                <v-card-text v-else class="px-0 py-0">
                  <a :title="$t('exoplatform.kudos.label.remainingKudos', (remainingKudos -1))" href="javascript:void(0);">X {{ remainingKudos - 1 }}</a>
                </v-card-text>
              </v-card>
            </v-layout>
          </v-container>
          <div v-if="remainingKudos <= 0" class="alert alert-info mt-5">
            <i class="uiIconInfo"></i>
            {{ $t('exoplatform.kudos.info.noKudosLeft', {0: remainingDaysToReset, 1: remainingDaysToReset === 1 ? $t('exoplatform.kudos.label.day') : $t('exoplatform.kudos.label.days') }) }}
          </div>
          <v-form v-else-if="kudosToSend" ref="form">
            <v-textarea
              id="kudosMessage"
              v-model="kudosMessage"
              :disabled="loading"
              :rules="kudosMessageRules"
              :label="$t('exoplatform.kudos.label.kudosMessage')"
              :placeholder="$t('exoplatform.kudos.label.kudosMessagePlaceholder')"
              name="kudosMessage"
              class="mb-0"
              rows="3"
              flat
              no-resize />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <button
            v-if="kudosToSend"
            :disabled="loading || error"
            class="ignore-vuetify-classes btn btn-primary me-3"
            @click="send">
            {{ $t('exoplatform.kudos.button.send') }}
          </button>
          <button
            :disabled="loading"
            class="ignore-vuetify-classes btn"
            @click="$refs.sendKudosModal.close()">
            {{ $t('exoplatform.kudos.button.close') }}
          </button>
          <v-spacer />
        </v-card-actions>
      </v-card>
    </exo-modal>

    <exo-modal
      ref="kudosListModal"
      :title="$t('exoplatform.kudos.label.kudosList')"
      width="500px"
      hide-actions
      @dialog-opened="listDialog = true"
      @dialog-closed="listDialog = false">
      <v-card flat>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>{{ error }}
        </div>
        <v-card-text>
          <v-container
            v-if="kudosList && kudosList.length"
            flat
            fluid
            grid-list-lg
            class="pa-0">
            <v-layout
              row
              wrap
              class="kudosIconsContainer">
              <v-card
                v-for="(kudos, index) in kudosList"
                :key="index"
                :class="kudos.isCurrent && 'kudosIconContainerCurrent'"
                flat
                class="text-center kudosIconContainerTop">
                <v-card-text v-if="kudos.senderFullName" class="kudosIconContainer">
                  <v-icon class="uiIconKudos uiIconBlue" size="64">fa-award</v-icon>
                  <v-icon class="uiIconKudosCheck uiIconBlue" size="16">fa-check-circle</v-icon>
                </v-card-text>
                <v-card-text v-if="kudos.senderFullName" class="kudosIconLink">
                  <kudos-identity-link
                    :id="kudos.senderId"
                    :technical-id="kudos.senderIdentityId"
                    :type="kudos.senderType"
                    :name="kudos.senderFullName" />
                </v-card-text>
              </v-card>
            </v-layout>
          </v-container>
          <div v-else class="alert alert-info">
            <i class="uiIconInfo"></i>
            {{ $t('exoplatform.kudos.info.noKudosOnActivity') }}
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <button class="ignore-vuetify-classes btn" @click="$refs.kudosListModal.close()">{{ $t('exoplatform.kudos.button.close') }}</button>
          <v-spacer />
        </v-card-actions>
      </v-card>
    </exo-modal>
  </v-app>
</template>

<script>
import {getReceiver} from '../../js/KudosIdentity.js';
import {getEntityKudos, sendKudos, getKudosSent} from '../../js/Kudos.js';
import {initSettings} from '../../js/KudosSettings.js';

export default {
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
        (v) => !!v || this.$t('exoplatform.kudos.warning.requiredField'),
        (v) => (v && this.escapeCharacters(v).replace(/ /g, '').length > 9) || this.$t('exoplatform.kudos.warning.atLeastTenCharacters'),
        (v) => (v && this.escapeCharacters(v).split(' ').length > 2) || this.$t('exoplatform.kudos.warning.atLeastThreeWords'),
      ],
    };
  },
  watch: {
    listDialog() {
      if (!this.listDialog || !this.entityId || !this.entityType) {
        return;
      }
      const $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${this.entityType}${this.entityId}`);
      const kudosList = $sendKudosLink.data('kudosList');
      if (!kudosList || !kudosList.length) {
        return;
      }
      this.kudosList = kudosList;
    },
    dialog() {
      if (!this.dialog) {
        return;
      }
      this.kudosMessage = null;
      this.kudosToSend = null;
      this.error = null;
      if (this.entityId && this.entityType) {
        this.allKudos = this.allKudosSent.slice(0);
        if (this.remainingKudos > 0) {
          getReceiver(this.entityType, this.entityId)
            .then(receiverDetails => {
              if (receiverDetails && receiverDetails.id && receiverDetails.type) {
                receiverDetails.isUserType = receiverDetails.type === 'organization' || receiverDetails.type === 'user';
                if (!receiverDetails.isUserType || receiverDetails.id !== eXo.env.portal.userName) {
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
                  if (receiverDetails.entityId) {
                    this.entityId = receiverDetails.entityId;
                  }
                  if (receiverDetails.notAuthorized) {
                    this.error = this.$t('exoplatform.kudos.warning.userNotAuthorizedToReceiveKudos');
                  } else {
                    this.kudosToSend = kudosToSend;
                  }
                  this.allKudos.push(kudosToSend);
                  if (this.remainingKudos > 1) {
                    this.allKudos.push({});
                  }
                  this.$nextTick(() => {
                    if ($('.kudosIconContainerTop.kudosIconContainerCurrent').length) {
                      $('.kudosIconContainerTop.kudosIconContainerCurrent')[0].scrollIntoView();
                    }
                  });
                } else {
                  throw new Error(this.$t('exoplatform.kudos.warning.cantSendKudosToYourSelf'));
                }
              } else {
                console.error('Receiver not found for entity type/id', this.entityType, this.entityId, receiverDetails);
                throw new Error(this.$t('exoplatform.kudos.error.errorGettingReceiverInformation'));
              }
            })
            .catch(e => {
              this.error = String(e);
              console.error('Error retrieving entity details with type and id', this.entityType, this.entityId, e);
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
          const limit = Math.max(20, window.kudosSettings.kudosPerPeriod);
          getKudosSent(eXo.env.portal.userIdentityId, limit)
            .then(allKudos => {
              this.allKudosSent = allKudos && allKudos.kudos || [];
            });
        })
        .catch(e => {
          this.error = e;
        });
    },
    refreshLink(element, entityType, entityId) {
      if (this.ignoreRefresh) {
        return Promise.resolve(null);
      }
      return getEntityKudos(entityType, entityId)
        .then(kudosList => {
          const $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${entityType}${entityId}`);
          $sendKudosLink.data('kudosList', kudosList);
          this.kudosList = kudosList;
        });
    },
    openDialog(event) {
      if (!this.disabled) {
        this.error = null;
        this.$refs.sendKudosModal.close();
        this.$nextTick(() => {
          this.entityType = event && event.detail && event.detail.type;
          this.entityId = event && event.detail && event.detail.id;
          this.parentEntityId = event && event.detail && event.detail.parentId;
          this.ignoreRefresh = event && event.detail && event.detail.ignoreRefresh;
          this.$refs.sendKudosModal.open();
        });
      }
    },
    openListDialog(event) {
      if (!this.disabled) {
        this.error = null;
        this.kudosList = [];
        this.entityType = event && event.detail && event.detail.type;
        this.entityId = event && event.detail && event.detail.id;
        this.refreshLink(null, this.entityType, this.entityId, '');
        this.$refs.kudosListModal.open();
      }
    },
    send() {
      this.error = null;

      if (!this.$refs.form.validate()) {
        return;
      }

      this.loading = true;
      const kudos = {
        entityType: this.entityType,
        entityId: this.entityId,
        parentEntityId: this.parentEntityId,
        receiverType: this.receiverType,
        receiverId: this.receiverId,
        message: this.kudosMessage
      };
      sendKudos(kudos)
        .then(kudosSent => {
          if (!kudosSent) {
            throw new Error(this.$t('exoplatform.kudos.error.errorSendingKudos'));
          }
          document.dispatchEvent(new CustomEvent('exo-kudos-sent', {detail: kudosSent}));
          return this.init()
            .catch(e => {
              console.error('Error refreshing allowed number of kudos for current user', e);
            });
        })
        .then(() => {
          return this.refreshLink(null, this.entityType, this.entityId, this.parentEntityId)
            .catch(e => {
              console.error('Error refreshing number of kudos', e);
            });
        })
        .then(() => this.$refs.sendKudosModal.close())
        .catch(e => {
          console.error('Error refreshing UI', e);
          this.error = String(e);
        })
        .finally(() => {
          this.loading = false;
          if (!this.dialog) {
            if (this.entityType === 'ACTIVITY') {
              this.refreshActivity(this.entityId);
            } else if (this.entityType === 'COMMENT') {
              let activityId = $(`#commentContainercomment${this.entityId}`).closest('.activityStream').attr('id');
              if (activityId) {
                const thiss = this;
                activityId = activityId.replace('activityContainer', '');
                thiss.refreshActivity(activityId);
                setTimeout(() => {
                  let commentToScrollTo = $(`[data-parent-comment=comment${this.entityId}]`).last();
                  if (commentToScrollTo && commentToScrollTo.is(':visible')) {
                    commentToScrollTo = commentToScrollTo.replace('commentContainer', '');
                    window.require(['SHARED/social-ui-activity'], (UIActivity) => {
                      UIActivity.focusToComment(commentToScrollTo);
                    });
                  }
                }, 400);
              }
            } else {
              if ($('.activityStreamStatus .uiIconRefresh').length && $('.activityStreamStatus .uiIconRefresh').is(':visible')) {
                $('.activityStreamStatus .uiIconRefresh').click();
              }
            }
          }
        });
    },
    getRemainingDays() {
      const remainingDateInMillis = window.kudosSettings.endPeriodDateInSeconds * 1000 - Date.now();
      if (remainingDateInMillis < 0) {
        return 0;
      }
      return parseInt(remainingDateInMillis / 86400000) + 1;
    },
    refreshActivity(activityId) {
      const $activityItem = $(`#UIActivityLoader${activityId}`);
      if ($activityItem.length && $activityItem.is('visible')) {
        $activityItem.data('url', $('.uiActivitiesLoaderURL').data('url'));
        $activityItem.addClass('activity-loadding');
        window.require(['SHARED/social-ui-activities-loader'], (UIActivityLoader) => {
          UIActivityLoader.renderActivity($activityItem);
        });
      }
    },
    escapeCharacters(value) {
      return value.replace(/((\r\n)|\n|\r)/g, '').replace(/(\.|,|\?|!)/g, ' ').replace(/( )+/g, ' ').trim();
    },
  }
};
</script>