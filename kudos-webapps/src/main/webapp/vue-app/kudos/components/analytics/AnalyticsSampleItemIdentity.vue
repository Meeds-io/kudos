<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
  <div class="text--secondary">
    {{ value }}
    (<a
      v-if="displayName"
      v-identity-popover="userIdentity"
      class="text-truncate"
      rel="nofollow"
      target="_blank">
      {{ displayName }}
    </a>
    <code v-else>
      {{ identityId }}
    </code>)
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
    chartData: {
      type: Object,
      default: () => null,
    },
    users: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  computed: {
    displayName() {
      return this.userIdentity?.fullName;
    },
    userIdentity() {
      if (this.value && this.users) {
        const userObj = this.users[this.value];
        if (userObj) {
          return {
            id: userObj.identityId,
            username: userObj.remoteId,
            fullName: userObj.displayName,
            position: userObj.position,
            avatar: userObj.avatarUrl,
            external: userObj.isExternal,
          };
        } else {
          return {
            identityId: this.value,
          };
        }
      } else {
        return null;
      }
    },
  },
};
</script>