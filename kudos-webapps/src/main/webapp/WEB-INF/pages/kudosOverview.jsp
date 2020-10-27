<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  String profileOwnerId = Utils.getOwnerIdentityId();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light kudosOverviewApplication"
    id="KudosOverview">
    <v-cacheable-dom-app cache-id="KudosOverview_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/kudos/KudosOverview'], app => app.init());
    </script>
  </div>
</div>