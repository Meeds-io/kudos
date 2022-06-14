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
          class="flex mx-4">
          <div class="d-flex flex-column flex-grow-1">
            <div class="d-flex flex-row pt-5 align-center">
              <span class="text-header-title">{{ $t('exoplatform.kudos.content.to') }}</span>
              <div v-if="!isLinkedKudos" class="d-flex pr-2 pl-5">
                <v-chip
                  class="identitySuggesterItem me-4">
                  <v-avatar left>
                    <v-img :src="receiverAvatarUrl" />
                  </v-avatar>
                  <span class="text-truncate">
                    {{ receiverFullName }}
                  </span>
                </v-chip>
              </div>
              <div
                v-else
                class="d-flex flex-row pl-4 kudosReceiverAttendeeItem">
                <div v-if="selectedReceiver" class="d-flex pr-2 pl-2 my-3 identitySuggesterInputStyle">
                  <v-chip
                    close
                    @click:close="removeReceiver"
                    class="identitySuggesterItem me-4 mt-2 mb-1">
                    <v-avatar left>
                      <v-img :src="receiverAvatarUrl" />
                    </v-avatar>
                    <span class="text-truncate">
                      {{ receiverFullName }}
                    </span>
                  </v-chip>
                </div>
                <exo-identity-suggester
                  v-else
                  ref="invitedAttendeeAutoComplete"
                  id="invitedAttendeeAutoComplete"
                  v-model="selectedReceiver"
                  :search-options="searchOptions"
                  type-of-relations="member_of_space"
                  name="inviteAttendee"
                  include-users />
              </div>
            </div>
            <div v-if="!isLinkedKudos">
              <div class="d-flex flex-row pt-5">
                <span class="text-header-title">{{ $t('exoplatform.kudos.choose.audience') }} </span>
              </div>
              <div class="d-flex flex-row pt-3">
                <exo-identity-suggester
                  ref="calendarOwnerSuggester"
                  v-model="audience"
                  :labels="spaceSuggesterLabels"
                  :include-users="false"
                  :width="220"
                  name="calendarOwnerAutocomplete"
                  class="user-suggester calendarOwnerAutocomplete"
                  include-spaces
                  only-redactor
                  required />
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
            <div class="d-flex flex-row pt-3">
              <span v-sanitized-html="$t('exooplatform.kudos.label.numberOfKudosAllowed', $t(KudosAllowedInfo))"></span>
            </div>
            <div v-if="kudosSent" class="d-flex flex-row pt-3">
              <span
                @click="handler($event)"
                v-sanitized-html="$t('exooplatform.kudos.label.numberOfKudosSent', $t(KudosSentInfo))"></span>
            </div>
          </div>
        </div>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            class="btn me-2"
            :aria-label="$t('Confirmation.label.Cancel')"
            @click="$refs.activityKudosDrawer.close()">
            {{ $t('Confirmation.label.Cancel') }}
          </v-btn>
          <v-btn
            :disabled="sendButtonDisabled"
            :aria-label="$t('exoplatform.kudos.button.send')"
            class="btn btn-primary me-2"
            @click="send">
            {{ $t('exoplatform.kudos.button.send') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
    <kudos-overview-drawer
      ref="kudosOverviewDrawer" />
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
      numberOfKudosToDisplay: 3,
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
      entityOwner: '',
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
      kudosPeriodType: '',
      loading: false,
      requiredField: false,
      identity: null,
      currentUserId: eXo.env.portal.userIdentityId,
      selectedReceiver: null,
      isEditReceiver: false,
      spaceURL: null,
      audience: ''
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
    },
    selectedReceiver(selectedReceiver) {
      if (selectedReceiver) {
        this.isEditReceiver = false;
        this.receiverId = selectedReceiver.remoteId;
        this.identity = {
          avatar: selectedReceiver.profile.avatarUrl,
          external: selectedReceiver.profile.external,
          fullname: selectedReceiver.profile.fullName,
          id: selectedReceiver.remoteId,
          identityId: selectedReceiver.identityId,
          isUserType: true,
          type: 'user',
          username: selectedReceiver.remoteId};
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

        document.addEventListener('exo-kudos-open-send-modal', this.openDrawer);
      });
    // Close user suggester list when clicking outside
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents(`.${this.invitedAttendeeAutoComplete}`).length && this.selectedReceiver) {
        this.isEditReceiver = false;
      }
    });
  },
  computed: {
    receiverFullName() {
      return this.selectedReceiver?.profile?.fullName;
    },
    receiverAvatarUrl() {
      return this.selectedReceiver?.profile?.avatarUrl;
    },
    searchOptions() {
      return {
        currentUser: eXo.env.portal.userName,
        spaceURL: this.spaceURL
      };
    },
    spaceSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('exoplatform.kudos.audience.searchPlaceholder'),
        placeholder: this.$t('exoplatform.kudos.audience.placeholder'),
        noDataLabel: this.$t('exoplatform.kudos.audience.noDataLabel'),
      };
    },
    KudosAllowedInfo() {
      return {
        0: `<span class="font-weight-bold">${this.numberOfKudosAllowed} ${this.$t('exoplatform.kudos.label.kudos')}</span>`,
        1: `<span class="font-weight-bold">${this.kudosPeriodLabel}</span>`,
      };
    },
    KudosSentInfo() {
      return {
        0: `<a class="font-weight-bold primary--text">${this.kudosSent} ${this.$t('exoplatform.kudos.label.kudos')}</a>`,
        1: `<span class="font-weight-bold">${this.kudosPeriodLabel}</span>`,
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
    kudosPeriodLabel () {
      return this.$t(`exoplatform.kudos.label.${this.kudosPeriodType}`);
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
    isLinkedKudos() {
      return this.entityType === 'ACTIVITY' || this.entityType === 'COMMENT';
    }
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
          getKudosSent(this.currentUserId, limit)
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
                  this.selectedReceiver = {
                    receiverId: receiverDetails.id,
                    id: `organization:${receiverDetails.id}`,
                    identityId: receiverDetails.identityId,
                    profile: {
                      fullName: receiverDetails.fullname,
                      avatarUrl: receiverDetails.avatar,
                      external: receiverDetails.external === 'true',
                    },
                    providerId: 'organization',
                    remoteId: receiverDetails.id
                  };
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
            this.entityOwner = event && event.detail && event.detail.owner;
            this.parentEntityId = event && event.detail && event.detail.parentId;
            this.ignoreRefresh = event && event.detail && event.detail.ignoreRefresh;
            this.spaceURL = event && event.detail && event.detail.spaceURL;
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
    send() {
      this.error = null;

      this.$refs.activityKudosDrawer.startLoading();
      const kudos = {
        entityType: this.entityType,
        entityId: this.entityId,
        parentEntityId: this.parentEntityId,
        receiverType: this.receiverType,
        receiverId: this.receiverId,
        message: this.kudosMessage,
        spacePrettyName: this.audience?.spaceId
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
    openSentKudos() {
      if (this.currentUserId) {
        this.$refs.kudosOverviewDrawer.open(this.$t('exoplatform.kudos.button.sentKudos'), 'sent', this.currentUserId, this.kudosPeriodType);
      }
    },
    handler(evt) {
      if (evt.target && evt.target.closest('a')) {
        this.openSentKudos();
      }
    },
    removeReceiver() {
      this.selectedReceiver = null;
      window.setTimeout(() => {
        if (this.$refs.invitedAttendeeAutoComplete){
          this.$refs.invitedAttendeeAutoComplete.focus();
        }
      }, 200);
    }
  }
};
</script>
