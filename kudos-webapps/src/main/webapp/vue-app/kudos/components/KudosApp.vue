<template>
  <v-app
    v-if="!disabled"
    id="KudosApp"
    color="transaprent"
    class="VuetifyApp"
    flat>
    <kudos-api ref="kudosAPI" />

    <exo-drawer
      ref="drawer"
      v-model="drawer"
      v-draggable="enabled"
      width="500px"
      hide-actions
      id="activityKudosDrawer"
      right
      disable-pull-to-refresh
      allow-expand
      @closed="closeDrawer">
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
            <div v-if="isLinkedKudos || !noReceiverIdentityId">
              <div class="d-flex flex-row pt-5 pb-3 align-center">
                <span class="text-header-title text-no-wrap">{{ $t('exoplatform.kudos.content.to') }}</span>
                <div
                  v-if="isLinkedKudos"
                  class="d-flex flex-row pl-4 text-truncate kudosReceiverAttendeeItem">
                  <exo-identity-suggester
                    ref="kudosReceiverAutoComplete"
                    id="kudosReceiverAutoComplete"
                    v-model="selectedReceiver"
                    :labels="receiverSuggesterLabels"
                    :search-options="searchOptions"
                    :type-of-relations="typeOfRelation"
                    include-users
                    name="kudosReceiver"
                    width="220"
                    class="user-suggester mb-2" />
                </div>
                <exo-user-avatar
                  v-else
                  class="d-flex flex-row pl-4"
                  :identity="identity"
                  :size="32"
                  :popover="false" />
              </div>
            </div>
            <div v-else>
              <div class="d-flex flex-column pt-5 pb-3">
                <span class="subtitle-1 text-color text-no-wrap">{{ $t('exoplatform.kudos.receiver.title') }}</span>
                <exo-identity-suggester
                  v-if="!selectedReceiver"
                  ref="kudosReceiver"
                  id="kudosReceiver"
                  v-model="selectedReceiver"
                  :labels="receiverSuggesterLabels"
                  :search-options="searchOptions"
                  :type-of-relations="typeOfRelation"
                  include-users
                  name="kudosReceiver"
                  width="220"
                  class="user-suggester" />
                <div v-else class="pt-1">
                  <v-chip
                    class="primary"
                    close
                    @click:close="removeReceiver">
                    <v-avatar left>
                      <v-img :src="selectedReceiver.profile.avatarUrl" role="presentation" />
                    </v-avatar>
                    <span class="text-truncate">
                      {{ selectedReceiver.profile.fullName }}
                    </span>
                  </v-chip>
                </div>
              </div>
            </div>
            <div v-if="audienceTypesDisplay" class="pt-4">
              <span class="subtitle-1 text-color"> {{ $t('exoplatform.kudos.visibility.title') }} </span>
              <v-radio-group
                v-model="audienceChoice"
                class="mt-0 mb-7"
                mandatory>
                <v-radio value="yourNetwork">
                  <template #label>
                    <span class="text-color text-subtitle-2 ms-1"> {{ $t('exoplatform.kudos.visibility.yourNetwork') }}</span>
                  </template>
                </v-radio>
                <v-radio value="oneOfYourSpaces">
                  <template #label>
                    <span class="text-color text-subtitle-2 ms-1"> {{ $t('exoplatform.kudos.visibility.oneOfYourSpaces') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
              <exo-identity-suggester
                v-if="spaceSuggesterDisplay"
                ref="audienceSuggester"
                v-model="audience"
                :labels="spaceSuggesterLabels"
                :include-users="false"
                :width="220"
                name="audienceAutocomplete"
                class="user-suggester mt-n2"
                include-spaces
                only-redactor />
            </div>
            <div class="d-flex flex-row">
              <v-list-item v-if="audienceAvatarDisplay" class="text-truncate px-0 mt-n1">
                <exo-space-avatar
                  :space-id="spaceId"
                  :size="30"
                  extra-class="text-truncate"
                  avatar />
                <exo-user-avatar
                  :profile-id="username"
                  :size="spaceId && 25 || 30"
                  :extra-class="spaceId && 'ms-n4 mt-6' || ''"
                  avatar />
                <v-list-item-content class="py-0 accountTitleLabel text-truncate">
                  <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
                    <exo-space-avatar
                      :space-id="spaceId"
                      :space="space"
                      extra-class="text-truncate"
                      fullname
                      bold-title
                      link-style
                      username-class />
                  </v-list-item-title>
                  <v-list-item-subtitle class="d-flex flex-row flex-nowrap">
                    <exo-user-avatar
                      :profile-id="username"
                      extra-class="text-truncate ms-2 me-1"
                      fullname
                      link-style
                      small-font-size
                      username-class />
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action v-if="!readOnlySpace" class="my-0">
                  <v-tooltip bottom>
                    <template #activator="{ on, attrs }">
                      <v-btn
                        icon
                        v-bind="attrs"
                        v-on="on"
                        @click="resetAudienceChoice()">
                        <v-icon size="14">
                          fas fa-redo
                        </v-icon>
                      </v-btn>
                    </template>
                    <span>
                      {{ $t('exoplatform.kudos.audience.reset.tooltip') }}
                    </span>
                  </v-tooltip>
                </v-list-item-action>
              </v-list-item>
            </div>
            <exo-user-avatar
              v-if="displaySenderAvatar"
              :profile-id="username"
              extra-class="text-truncate"
              link-style
              small-font-size
              username-class />
            <div class="d-flex flex-row pt-8">
              <rich-editor
                v-if="drawer"
                ref="kudosContent"
                v-model="kudosMessage"
                :max-length="MESSAGE_MAX_LENGTH"
                :ck-editor-type="ckEditorType"
                :ck-editor-id="ckEditorId"
                :placeholder="$t('exoplatform.kudos.label.kudosMessagePlaceholder')"
                :suggestor-type-of-relation="typeOfRelation"
                :suggester-space-pretty-name="spacePrettyName"
                :object-id="metadataObjectId"
                :object-type="objectType"
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
            @click="$refs.drawer.close()">
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
      metadataObjectId: null,
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
      audience: '',
      objectType: 'activity',
      readOnlySpace: false,
      username: eXo.env.portal.userName,
      spaceId: eXo.env.portal.spaceId,
      spacePrettyName: eXo.env.portal.spaceName,
      audienceChoice: null,
      noReceiverIdentityId: false,
      isPublicSite: eXo.env.portal.portalName === 'public',
    };
  },
  watch: {
    error() {
      if (this.error) {
        this.displayAlert(this.error, 'error');
      } else {
        this.closeAlert();
      }
    },
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
        if (this.receiverId !== selectedReceiver.remoteId) {
          this.receiverId = selectedReceiver.remoteId;
        }
      }
    },
    audience() {
      this.spacePrettyName = this.audience?.remoteId || null;
      this.spaceId = this.audience?.spaceId || null;
    },
    audienceChoice(newVal) {
      if (newVal === 'yourNetwork') {
        this.removeAudience();
      }
    },
    entityType(newVal) {
      if (newVal === 'USER_TIPTIP') {
        this.spaceId = null;
      }
    }
  },
  created() {
    if (!this.isPublicSite) {
      this.init()
        .then(() => {
          if (this.disabled) {
            return;
          }
          this.$refs.kudosAPI.init();
          document.addEventListener('exo-kudos-open-send-modal', this.openDrawer);
        });
    }
  },
  computed: {
    searchOptions() {
      return {
        currentUser: this.username,
        spacePrettyName: this.spacePrettyName,
        activityId: this.entityId
      };
    },
    spaceSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('exoplatform.kudos.audience.searchPlaceholder'),
        placeholder: this.$t('exoplatform.kudos.audience.placeholder'),
        noDataLabel: this.$t('exoplatform.kudos.audience.noDataLabel'),
      };
    },
    receiverSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('exoplatform.kudos.receiver.searchPlaceholder'),
        placeholder: this.$t('exoplatform.kudos.receiver.placeholder'),
        noDataLabel: this.spacePrettyName ? this.$t('exoplatform.kudos.receiver.noDataLabelInSpace') : this.$t('exoplatform.kudos.receiver.noDataLabel'),
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
      return !this.kudosMessageText|| this.kudosMessageTextLength > this.MESSAGE_MAX_LENGTH || this.kudosMessageValidityLabel || (this.postInYourSpacesChoice && !this.audience) || (this.noReceiverIdentityId && !this.selectedReceiver);
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
    },
    typeOfRelation() {
      return this.isLinkedKudos ? 'mention_comment' : 'mention_activity_stream';
    },
    ckEditorType() {
      return this.isLinkedKudos ? 'activityComment' : 'activityContent';
    },
    enabled() {
      return eXo.env.portal.editorAttachImageEnabled && eXo.env.portal.attachmentObjectTypes?.indexOf(this.objectType) >= 0;
    },
    postInYourSpacesChoice() {
      return this.audienceChoice === 'oneOfYourSpaces';
    },
    postInYourNetwork() {
      return this.audienceChoice === 'yourNetwork';
    },
    spaceSuggesterDisplay() {
      return this.postInYourSpacesChoice && !this.audience;
    },
    audienceAvatarDisplay() {
      return (this.audience && this.postInYourSpacesChoice) || this.readOnlySpace;
    },
    audienceTypesDisplay() {
      return (!this.spaceId && !this.isLinkedKudos) || (!this.spaceId && !this.readOnlySpace && !this.isLinkedKudos) || (!this.readOnlySpace  && this.postInYourSpacesChoice && !this.audience) || (!this.isLinkedKudos && !this.noReceiverIdentityId && !this.audienceAvatarDisplay);
    },
    displaySenderAvatar() {
      return (this.postInYourNetwork && this.audienceTypesDisplay) || this.isLinkedKudos;
    },
    ckEditorInstance() {
      return this.drawer && this.$refs.kudosContent || null;
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
          getKudosSent(this.currentUserId, limit)
            .then(allKudos => {
              this.allKudosSent = allKudos && allKudos.kudos || [];
            });
        })
        .catch(e => {
          this.error = e;
        });
    },
    closeDrawer() {
      this.resetAudienceChoice();
      this.ckEditorInstance.destroyCKEditor();
    },
    initDrawer() {
      this.kudosMessage = '';
      this.kudosToSend = null;
      this.error = null;
      this.requiredField = false;
      let kudosToSend = null;
      this.allKudos = this.allKudosSent.slice(0);
      if (this.remainingKudos > 0) {
        return (this.entityId && this.entityType && getReceiver(this.entityType, this.entityId) || Promise.resolve(null))
          .then(receiverDetails => {
            this.noReceiverIdentityId = !receiverDetails;
            if (receiverDetails?.id && receiverDetails?.type) {
              receiverDetails.isUserType = receiverDetails.type === 'organization' || receiverDetails.type === 'user';
              if (!receiverDetails.isUserType || receiverDetails.id !== this.username) {
                if (this.isLinkedKudos) {
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
                } else {
                  this.identity = receiverDetails;
                }
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
                } else {
                  this.noReceiverIdentityId = true;
                  if (!receiverDetails.identityId) {
                    this.selectedReceiver = null;
                  }
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
                return this.$nextTick().then(() => {
                  if ($('.kudosIconContainerTop.kudosIconContainerCurrent').length) {
                    $('.kudosIconContainerTop.kudosIconContainerCurrent')[0].scrollIntoView();
                  }
                });
              }
            }
            this.entityOwner = null;
          })
          .catch(e => {
            this.error = String(e);
            console.error('Error retrieving entity details with type and id', this.entityType, this.entityId, e);
          });
      }
    },
    refreshLink(element, entityType, entityId) {
      if (this.ignoreRefresh) {
        return Promise.resolve(null);
      }
      this.$refs.drawer.startLoading();
      return getEntityKudos(entityType, entityId)
        .then(kudosList => {
          const $sendKudosLink = $(window.parentToWatch).find(`#SendKudosButton${entityType}${entityId}`);
          $sendKudosLink.data('kudosList', kudosList);
          this.kudosList = kudosList;
        })
        .finally(() =>  this.$refs.drawer.endLoading()
        );
    },
    openDrawer(event) {
      if (!this.disabled) {
        if ( this.remainingKudos > 0 ) {
          this.loading = true;
          this.$nextTick(() => {
            this.readOnlySpace = event?.detail?.readOnlySpace;
            this.entityType = event?.detail?.type || 'NONE';
            this.entityId = event?.detail?.id || 0;
            this.entityOwner = event?.detail?.owner || eXo.env.portal.userName;
            this.parentEntityId = event?.detail?.parentId || '';
            this.ignoreRefresh = event && event.detail && event.detail.ignoreRefresh;
            this.spacePrettyName = event && event.detail && event.detail.spaceURL || null;
            this.metadataObjectId = null;
            this.$refs.drawer.open();
            this.$refs.drawer.startLoading();
            Promise.resolve(this.initDrawer())
              .finally(() => this.$nextTick())
              .then(() => this.ckEditorInstance.initCKEditor())
              .finally( () => {
                this.loading = false;
                this.$refs.drawer.endLoading();
              });
          });
        } else {
          this.displayAlert(this.$t('exoplatform.kudos.info.noKudosLeft', {
            0: this.remainingDaysToReset,
            1: this.remainingPeriodLabel
          }), 'warning');
        }
      }
    },
    send() {
      this.error = null;

      const kudosMessage = this.ckEditorInstance.getMessage();
      this.$refs.drawer.startLoading();
      const kudos = {
        entityType: this.entityType,
        entityId: this.entityId,
        parentEntityId: this.parentEntityId,
        receiverType: this.receiverType || 'user',
        receiverId: this.receiverId,
        message: kudosMessage,
        spacePrettyName: this.audience?.remoteId || this.spacePrettyName,
      };
      sendKudos(kudos)
        .then(kudosSent => {
          if (!kudosSent) {
            throw new Error(this.$t('exoplatform.kudos.error.errorSendingKudos'));
          }
          this.metadataObjectId = this.isLinkedKudos && `comment${kudosSent.activityId}` || `${kudosSent.activityId}`;
          document.dispatchEvent(new CustomEvent('exo-kudos-sent', {detail: kudosSent}));
          return this.$nextTick();
        })
        .then(() => this.ckEditorInstance.saveAttachments())
        .then(() => {
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
          this.selectedReceiver = null;
          this.resetAudienceChoice();
          this.noReceiverIdentityId = false;
          this.$refs.drawer.close();
          this.displayAlert(this.$t('exoplatform.kudos.success.kudosSent'));
        })
        .catch(e => {
          console.error('Error refreshing UI', e);
          this.error = String(e);
        })
        .finally(() => {
          this.$refs.drawer.endLoading();
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
    closeAlert() {
      document.dispatchEvent(new CustomEvent('close-alert-message'));
    },
    displayAlert(message, type) {
      document.dispatchEvent(new CustomEvent('alert-message', {detail: {
        alertMessage: message,
        alertType: type || 'success',
      }}));
    },
    resetAudienceChoice() {
      this.audienceChoice = null;
      this.audience = '';
    },
    removeAudience() {
      this.audience = '';
    },
    removeReceiver() {
      this.selectedReceiver = '';
    }
  }
};
</script>
