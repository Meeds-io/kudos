<%
/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
%>
<%@ page import="org.exoplatform.services.security.ConversationState"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%@ page import="org.exoplatform.portal.config.model.Page"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.portal.config.UserACL"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%
  String profileOwnerId = Utils.getOwnerIdentityId();
  String portletStorageId = ((String) request.getAttribute("portletStorageId"));
  String kudosPeriod = request.getAttribute("kudosPeriod") == null ? "week" : ((String[]) request.getAttribute("kudosPeriod"))[0];
  Page currentPage = PortalRequestContext.getCurrentInstance().getPage();
  boolean canEdit = ExoContainerContext.getService(UserACL.class).hasEditPermission(currentPage, ConversationState.getCurrent().getIdentity());
  String pageRef = currentPage.getPageKey().format();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light kudosOverviewApplication"
    id="KudosOverview">
    <script type="text/javascript">
      require(['PORTLET/kudos/KudosOverview'], app => app.init(
        '<%=portletStorageId%>',
        '<%=kudosPeriod%>',
        <%=canEdit%>,
        '<%=pageRef%>'
      ));
    </script>
  </div>
</div>