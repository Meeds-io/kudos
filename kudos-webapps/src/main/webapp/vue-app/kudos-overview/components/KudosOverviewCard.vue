<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <v-row
    :class="clickable && 'clickable' || ''"
    class="border-box-sizing text-center mx-auto justify-center"
    @click="clickable && $emit('open-drawer')">
    <v-col class="my-auto px-0 col-md-auto kudosOverviewIcon">
      <v-icon class="tertiary-color uiIconAward" size="56" />
    </v-col>
    <v-col class="my-auto col-md-auto kudosOverviewCount text-color mx-0 display-2 font-weight-bold">
      <slot name="count"></slot>
    </v-col>
    <v-col class="my-auto col-12 col-md-1 text-md-left px-0 mx-0 kudosOverviewLabel text-color">
      <slot name="label"></slot>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: {
    clickable: {
      type: Boolean,
      default: null,
    },
    skeleton: {
      type: Boolean,
      default: null,
    },
  },
};
</script>