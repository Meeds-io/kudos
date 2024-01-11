package org.exoplatform.kudos.activity.processor;

import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.utils.MentionUtils;

public class ActivityKudosProcessor extends BaseActivityProcessorPlugin {

  private KudosService kudosService;

  private String       defaultPortal;

  public ActivityKudosProcessor(KudosService kudosService,
                                UserPortalConfigService userPortalConfigService,
                                InitParams initParams) {
    super(initParams);
    this.kudosService = kudosService;
    this.defaultPortal = userPortalConfigService.getMetaPortal();
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity.isComment()) {
      return;
    }
    if (activity.getLinkedProcessedEntities() == null) {
      activity.setLinkedProcessedEntities(new HashMap<>());
    }
    @SuppressWarnings("unchecked")
    List<Kudos> linkedKudosList = (List<Kudos>) activity.getLinkedProcessedEntities().get("kudosList");
    if (linkedKudosList == null) {
      linkedKudosList = kudosService.getKudosListOfActivity(activity.getId());
      activity.getLinkedProcessedEntities().put("kudosList", linkedKudosList);
    }

    if (linkedKudosList != null) {
      for (Kudos kudos : linkedKudosList) {
        kudos.setMessage(MentionUtils.substituteUsernames(defaultPortal, kudos.getMessage()));
      }
    }
  }

}
