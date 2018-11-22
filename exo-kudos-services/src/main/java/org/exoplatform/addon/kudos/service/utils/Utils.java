package org.exoplatform.addon.kudos.service.utils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class Utils {

  public static final String SPACE_ACCOUNT_TYPE        = "space";

  public static final String USER_ACCOUNT_TYPE         = "user";

  public static final String DEFAULT_ACCESS_PERMISSION = "defaultAccessPermission";

  public static final String DEFAULT_KUDOS_PER_MONTH   = "defaultKudosPerMonth";

  public static Space getSpace(String id) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (id.indexOf("/spaces/") >= 0) {
      return spaceService.getSpaceByGroupId(id);
    }
    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null) {
      space = spaceService.getSpaceByGroupId("/spaces/" + id);
      if (space == null) {
        space = spaceService.getSpaceByDisplayName(id);
        if (space == null) {
          space = spaceService.getSpaceByUrl(id);
        }
      }
    }
    return space;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }

}
