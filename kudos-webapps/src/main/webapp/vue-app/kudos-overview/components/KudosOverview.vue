<template>
  <v-app :class="owner && 'kudosOverviewApplication' || 'kudosOverviewApplicationOther'">
    <widget-wrapper
      id="kudosOverviewHeader"
      extra-class="application-body hover-swap-trigger">
      <template #title>
        <div class="d-flex flex-grow-1 full-width position-relative">
          <div v-if="hasKudos && !loading" class="text-header text-truncate">
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
            <!-- The 'See all' text is replaced by the icons on hover or
                 keyboard focus: the helper classes keep the icons in the tab
                 order -->
            <div class="hover-swap-parent d-flex align-center">
              <v-btn
                v-if="hasKudos"
                :tabindex="$root.canEdit && -1 || null"
                :class="$root.canEdit && 'hover-swap-default'"
                class="pa-0"
                height="auto"
                min-width="auto"
                text
                @click="$root.$emit('kudos-overview-drawer', 'sent', ownerIdentityId, $root.kudosPeriod)">
                <span class="primary--text text-none">{{ $t('kudosOverview.seeAll') }}</span>
              </v-btn>
              <div
                v-if="$root.canEdit"
                class="hover-swap-alt align-center">
                <v-btn
                  v-if="hasKudos"
                  :title="$t('kudosOverview.seeAll')"
                  :aria-label="$t('kudosOverview.seeAll')"
                  small
                  icon
                  @click="$root.$emit('kudos-overview-drawer', 'sent', ownerIdentityId, $root.kudosPeriod)">
                  <v-icon
                    size="18"
                    color="primary">
                    fa-external-link-alt
                  </v-icon>
                </v-btn>
                <v-btn
                  :title="$t('kudosOverview.settings.editTooltip')"
                  :aria-label="$t('kudosOverview.settings.editTooltip')"
                  small
                  icon
                  @click="$root.$emit('kudos-overview-settings')">
                  <v-icon size="18">fa-cog</v-icon>
                </v-btn>
              </div>
            </div>
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
  }),
};
</script>