<template>
  <v-app :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'">
    <v-hover v-model="hover">
      <widget-wrapper
        id="kudosOverviewHeader"
        extra-class="application-body">
        <template #title>
          <div class="d-flex flex-grow-1 full-width position-relative">
            <div v-if="hasKudos && !loading" class="widget-text-header text-truncate">
              {{ $t('exoplatform.kudos.button.rewardedKudos') }}
            </div>
            <div class="spacer"></div>
            <div
              :class="{
                'mt-2 me-2': !hasKudos,
                'l-0': $vuetify.rtl,
                'r-0': !$vuetify.rtl,
              }"
              class="position-absolute absolute-vertical-center z-index-one">
              <v-btn
                :icon="hoverEdit"
                :small="hoverEdit"
                height="auto"
                min-width="auto"
                class="pa-0"
                text
                @click="$root.$emit('kudos-overview-drawer', 'sent', ownerIdentityId, hasKudos && $root.kudosPeriod || 'year')">
                <v-icon
                  v-if="hoverEdit"
                  size="18"
                  color="primary">
                  fa-external-link-alt
                </v-icon>
                <span v-else class="primary--text text-none">{{ $t('kudosOverview.seeAll') }}</span>
              </v-btn>
              <v-fab-transition hide-on-leave>
                <v-btn
                  v-show="hoverEdit"
                  :title="$t('kudosOverview.settings.editTooltip')"
                  small
                  icon
                  @click="$root.$emit('kudos-overview-settings')">
                  <v-icon size="18">fa-cog</v-icon>
                </v-btn>
              </v-fab-transition>
            </div>
          </div>
        </template>
        <template #default>
          <kudos-overview-row
            :period-type="$root.kudosPeriod"
            class="my-auto"
            @loading="loading = $event"
            @has-kudos="hasKudos = $event" />
        </template>
      </widget-wrapper>
    </v-hover>
    <kudos-overview-settings-drawer />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    ownerIdentityId: eXo.env.portal.profileOwnerIdentityId,
    loading: true,
    hasKudos: false,
    hover: false,
  }),
  computed: {
    hoverEdit() {
      return this.$root.canEdit && this.hover;
    },
  },
};
</script>