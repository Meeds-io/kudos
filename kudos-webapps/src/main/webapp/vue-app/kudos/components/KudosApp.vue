<template>
  <v-app
    v-if="!disabled"
    id="KudosApp"
    color="transaprent"
    class="VuetifyApp"
    flat>
    <kudos-api ref="kudosAPI" />
    <kudos-notification-alert />

    <exo-drawer
      ref="activityKudosDrawer"
      width="500px"
      hide-actions
      id="activityKudosDrawer"
      right
      disable-pull-to-refresh
      @closed="resetEditor">
      <template slot="title">
        <span class="text-header-title">
          {{ $t('exoplatform.kudos.title.sendAKudos') }}
        </span>
      </template>
      <template slot="content">
        <div
          v-show="!loading"
          ref="activityKudosForm"
          class="flex mx-4 pt-3">
          <div class="d-flex flex-column flex-grow-1">
            <div class="d-flex flex-row">
              <div class="d-flex flex-column flex-grow-1">
                <span class="text-header-title my-auto mt-7 text-no-wrap">{{ $t('exoplatform.kudos.content.to') }} </span>
              </div>
              <div class="d-flex flex-column pr-2 pt-3">
                <div class="d-flex flex-row pt-3">
                  <exo-user-avatar
                    :username="kudosReceiver.receiverId"
                    :fullname="kudosReceiver.fullName"
                    :avatar-url="kudosReceiver.avatar"
                    :url="kudosReceiver.profileUrl"
                    bold-title
                    link-style
                    size="32" />
                </div>
                <div class="d-flex flex-row">
                  <div>
                    <span class="text-sm-caption text-sub-title">
                      {{ $t('exooplatform.kudos.label.numberOfKudos', {0: numberOfKudosAllowed , 1: kudosPeriodType, 2: kudosSent , 3: numberOfKudosAllowed}) }}
                    </span>
                  </div>
                  <div
                    v-if="kudosSent || remainingKudos"
                    class="pl-9">
                    <v-icon
                      v-for="index in remainingKudos"
                      :key="index"
                      class="uiIconKudos uiIconBlue pl-1"
                      size="20">
                      fa-award
                    </v-icon>
                    <v-icon
                      v-for="index in kudosSent"
                      :key="index"
                      class="uiIconKudos uiIconGrey pl-1"
                      size="20">
                      fa-award
                    </v-icon>
                  </div>
                </div>
              </div>
            </div>
            <div class="d-flex flex-row pt-5">
              <span class="text-header-title">{{ $t('exoplatform.kudos.title.message') }} </span>
            </div>
            <div class="d-flex flex-row pt-3">
              <exo-activity-rich-editor
                :ref="ckEditorId"
                v-model="kudosMessage"
                :max-length="MESSAGE_MAX_LENGTH"
                :ck-editor-type="ckEditorId"
                :placeholder="$t('exoplatform.kudos.label.kudosMessagePlaceholder')"
                class="flex"
                autofocus />
            </div>
            <div v-if="kudosMessageValidityLabel" class="d-flex flex-row pt-3">
              <span class="text-sm-caption error--text">
                {{ kudosMessageValidityLabel }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            class="btn me-2"
            @click="$refs.activityKudosDrawer.close()">
            {{ $t('Confirmation.label.Cancel') }}
          </v-btn>
          <v-btn
            :disabled="sendButtonDisabled"
            class="btn btn-primary me-2"
            @click="send">
            {{ $t('exoplatform.kudos.button.send') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>

    <exo-modal
      ref="kudosListModal"
      v-show="listDialog"
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
          <div v-else-if="!loading" class="alert alert-info">
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
      numberOfKudosAllowed: 0,
      listDialog: false,
      ignoreRefresh: false,
      kudosList: false,
      disabled: false,
      noKudosLeft: false,
      remainingKudos: 0,
      remainingDaysToReset: 0,
      entityIds: [],
      parentEntityId: null,
      entityId: null,
      entityType: null,
      receiverType: null,
      receiverId: null,
      error: null,
      drawer: false,
      MESSAGE_MAX_LENGTH: 1300,
      ckEditorId: 'kudosContent',
      allKudosSent: [],
      allKudos: [],
      kudosToSend: null,
      kudosMessage: '',
      loading: false,
      requiredField: false,
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
    kudosMessageText(newVal, oldVal) {
      this.requiredField = oldVal && oldVal !== '' && newVal === '';
    }
  },
  created() {
    this.init()
      .then(() => {
        if (this.disabled) {
          return;
        }
        this.$refs.kudosAPI.init();

        document.addEventListener('exo-kudos-open-send-modal', this.openDrawer);
        document.addEventListener('exo-kudos-open-kudos-list', this.openListDialog);
      });
  },
  computed: {
    kudosReceiver () {
      return {
        receiverId: this.kudosToSend && this.kudosToSend.id,
        avatar: this.kudosToSend && this.kudosToSend.avatar,
        profileUrl: this.kudosToSend && this.kudosToSend.profileUrl,
        fullName: this.kudosToSend && this.kudosToSend.receiverFullName
      };
    },
    kudosSent () {
      return this.numberOfKudosAllowed - this.remainingKudos;
    },
    sendButtonDisabled() {
      return !this.kudosMessageText|| this.kudosMessageTextLength > this.MESSAGE_MAX_LENGTH || this.kudosMessageValidityLabel ;
    },
    remainingPeriodLabel() {
      return this.remainingDaysToReset === 1 ? this.$t('exoplatform.kudos.label.day') : this.$t('exoplatform.kudos.label.days') ;
    },
    kudosMessageText() {
      return this.kudosMessage && this.$utils.htmlToText(this.kudosMessage);
    },
    kudosMessageHasThreeWords() {
      return this.kudosMessageText && this.kudosMessageText.split(' ').length > 2 ;
    },
    isEmptyKudosMessage() {
      return this.kudosMessageText === '';
    },
    kudosMessageTextLength() {
      return this.kudosMessageText && this.kudosMessageText.length;
    },
    atLeastThreeWordsLabel() {
      return !this.isEmptyKudosMessage && !this.kudosMessageHasThreeWords && this.$t('exoplatform.kudos.warning.atLeastThreeWords');
    },
    requiredFieldLabel() {
      return this.requiredField && this.$t('exoplatform.kudos.warning.requiredField');
    },
    kudosMessageValidityLabel() {
      return this.requiredFieldLabel || this.atLeastThreeWordsLabel;
    },
  },
  methods: {
    init() {
      return initSettings()
        .then(() => {
          this.disabled = window.kudosSettings && window.kudosSettings.disabled;
          this.numberOfKudosAllowed = Number(window.kudosSettings && window.kudosSettings.kudosPerPeriod);
          this.remainingKudos = Number(window.kudosSettings && window.kudosSettings.remainingKudos);
          this.kudosPeriodType = window.kudosSettings && window.kudosSettings.kudosPeriodType.toLowerCase();
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
    resetEditor() {
      this.$refs[this.ckEditorId].destroyCKEditor();
    },
    initDrawer () {
      this.kudosMessage = '';
      this.kudosToSend = null;
      this.error = null;
      this.requiredField = false;
      let kudosToSend = null;
      if (this.entityId && this.entityType) {
        this.allKudos = this.allKudosSent.slice(0);
        if (this.remainingKudos > 0) {
          return getReceiver(this.entityType, this.entityId)
            .then(receiverDetails => {
              if (receiverDetails && receiverDetails.id && receiverDetails.type) {
                receiverDetails.isUserType = receiverDetails.type === 'organization' || receiverDetails.type === 'user';
                if (!receiverDetails.isUserType || receiverDetails.id !== eXo.env.portal.userName) {
                  this.receiverId = receiverDetails.id;
                  this.receiverType = receiverDetails.type;
                  const receiverId = receiverDetails.id;
                  kudosToSend = {
                    receiverId: receiverId,
                    receiverType: receiverDetails.type,
                    avatar: receiverDetails.avatar,
                    profileUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${receiverId}`,
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
    },
    refreshLink(element, entityType, entityId) {
      if (this.ignoreRefresh) {
        return Promise.resolve(null);
      }
      this.$refs.activityKudosDrawer.startLoading();
      return getEntityKudos(entityType, entityId)
        .then(kudosList => {
          const $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${entityType}${entityId}`);
          $sendKudosLink.data('kudosList', kudosList);
          this.kudosList = kudosList;
        })
        .finally(() =>  this.$refs.activityKudosDrawer.endLoading()
        );
    },
    openDrawer(event) {
      if (!this.disabled) {
        if ( this.remainingKudos > 0 ) {
          this.loading = true;
          this.$nextTick(() => {
            this.entityType = event && event.detail && event.detail.type;
            this.entityId = event && event.detail && event.detail.id;
            this.parentEntityId = event && event.detail && event.detail.parentId;
            this.ignoreRefresh = event && event.detail && event.detail.ignoreRefresh;
            this.$refs.activityKudosDrawer.open();
            this.$refs.activityKudosDrawer.startLoading();
            this.initDrawer().then(() => {
              this.$refs[this.ckEditorId].initCKEditor();
            }).finally( () => {
              this.loading = false;
              this.$refs.activityKudosDrawer.endLoading();
            });
          });
        }
        else {
          this.$root.$emit('kudos-notification-alert', {
            message: this.$t('exoplatform.kudos.info.noKudosLeft', {0: this.remainingDaysToReset, 1: this.remainingPeriodLabel}),
            type: 'warning',
          });
        }
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

      this.$refs.activityKudosDrawer.startLoading();
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
        .then(() => {
          this.$refs.activityKudosDrawer.close();
          if (this.entityType === 'COMMENT') {
            this.$root.$emit('kudos-notification-alert', {
              message: this.$t('exoplatform.kudos.success.kudosSent'),
              type: 'success',
            });
          }
        })
        .catch(e => {
          console.error('Error refreshing UI', e);
          this.error = String(e);
        })
        .finally(() => {
          this.$refs.activityKudosDrawer.endLoading();
        });
    },
    getRemainingDays() {
      const remainingDateInMillis = window.kudosSettings.endPeriodDateInSeconds * 1000 - Date.now();
      if (remainingDateInMillis < 0) {
        return 0;
      }
      return parseInt(remainingDateInMillis / 86400000) + 1;
    },
    escapeCharacters(value) {
      return value.replace(/((\r\n)|\n|\r)/g, '').replace(/(\.|,|\?|!)/g, ' ').replace(/( )+/g, ' ').trim();
    },
  }
};
</script>