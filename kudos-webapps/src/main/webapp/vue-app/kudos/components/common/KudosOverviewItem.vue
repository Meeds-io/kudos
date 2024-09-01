<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-list-item
    :href="activityUrl"
    two-line>
    <v-list-item-avatar :tile="isSpace" :class="isSpace && 'spaceAvatar'">
      <v-img
        :src="avatar" />
    </v-list-item-avatar>
    <v-list-item-content class="py-0">
      <v-list-item-title :id="id" class="font-weight-bold">
        {{ fullName }}
      </v-list-item-title>
      <v-list-item-subtitle>
        {{ dateTime }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-tooltip bottom>
      <template #activator="{attrs, on}">
        <v-list-item-icon
          v-bind="attrs"
          v-on="on"
          class="my-auto">
          <v-icon size="20">fa-eye</v-icon>
        </v-list-item-icon>
      </template>
      <span>{{ $t('kudosOverview.openKudosActivity') }}</span>
    </v-tooltip>
  </v-list-item>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    kudosItem: {
      type: Object,
      default: () => ({}),
    },
    type: {
      type: String,
      default: 'sent',
    },
  },
  data: () => ({
    id: `avatar${parseInt(Math.random() * randomMax)
      .toString()
      .toString()}`,
    lang: eXo.env.portal.language,
    dateFormat: {
      dateStyle: 'long',
      timeStyle: 'short',
    },
  }),
  computed: {
    isSender() {
      return this.kudosItem && Number(this.kudosItem.senderIdentityId) === Number(eXo.env.portal.profileOwnerIdentityId);
    },
    isSpace() {
      return this.isSender && this.kudosItem.receiverType === 'space';
    },
    avatar() {
      return this.isSender && this.kudosItem.receiverAvatar || this.kudosItem.senderAvatar;
    },
    fullName() {
      return this.isSender && this.kudosItem.receiverFullName || this.kudosItem.senderFullName;
    },
    remoteId() {
      return this.isSender && this.kudosItem.receiverId || this.kudosItem.senderId;
    },
    identityId() {
      return this.isSender && this.kudosItem.receiverIdentityId || this.kudosItem.senderIdentityId;
    },
    dateTime() {
      if (!this.kudosItem || !this.kudosItem.timeInSeconds) {
        return '';
      }
      const dateTime = new Date(this.kudosItem.timeInSeconds * 1000);
      return new window.Intl.DateTimeFormat(this.lang, this.dateFormat).format(dateTime);
    },
    activityUrl() {
      if (!this.kudosItem) {
        return null;
      }
      const activityId = this.kudosItem.entityType === 'COMMENT' && this.kudosItem.parentEntityId 
        || this.kudosItem.entityType === 'ACTIVITY' && this.kudosItem.entityId
        || this.kudosItem.activityId;
      let commentId = null;
      if (this.kudosItem.entityType === 'COMMENT' || this.kudosItem.entityType === 'ACTIVITY') {
        commentId = this.kudosItem.activityId;
      }
      return this.kudosItem && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${activityId}${commentId && '#comment-comment' || ''}${commentId || ''}`;
    },
  },
};
</script>
