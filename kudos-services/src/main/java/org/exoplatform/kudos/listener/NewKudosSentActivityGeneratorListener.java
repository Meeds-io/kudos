package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.processor.I18NActivityUtils;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.notification.Utils;

/**
 * A listener to add comment or activity
 */
public class NewKudosSentActivityGeneratorListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(NewKudosSentActivityGeneratorListener.class);

  private ActivityManager  activityManager;

  private ActivityStorage  activityStorage;

  public NewKudosSentActivityGeneratorListener(ActivityManager activityManager, ActivityStorage activityStorage) {
    this.activityStorage = activityStorage;
    this.activityManager = activityManager;
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    KudosService kudosService = event.getSource();
    if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.ACTIVITY
        || KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
      String activityId = kudos.getEntityId();
      try {
        String parentCommentId = null;
        ExoSocialActivity activity = null;
        if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
          ExoSocialActivity comment = this.activityManager.getActivity(ACTIVITY_COMMENT_ID_PREFIX + activityId);
          if (comment != null) {
            activity = this.activityManager.getParentActivity(comment);
            if (comment.getParentCommentId() != null) {
              parentCommentId = comment.getParentCommentId();
            } else {
              parentCommentId = comment.getId();
            }
          }
        } else {
          activity = this.activityManager.getActivity(activityId);
        }
        if (activity == null) {
          throw new IllegalStateException("Activity with id '" + activityId + "' wasn't found");
        }
        ExoSocialActivity activityComment = createActivity(kudos, parentCommentId);
        activityStorage.saveComment(activity, activityComment);
        kudosService.saveKudosActivity(kudos.getTechnicalId(), getActivityId(activityComment.getId()));
      } catch (Exception e) {
        LOG.warn("Error adding comment on activity with id '" + activityId + "' for Kudos with id " + kudos.getTechnicalId(), e);
      }
    } else {
      ExoSocialActivity activity = createActivity(kudos, null);
      String providerId = OrganizationIdentityProvider.NAME;
      if (SPACE_ACCOUNT_TYPE.equals(kudos.getReceiverType())) {
        providerId = SpaceIdentityProvider.NAME;
      }

      Identity owner = Utils.getIdentityManager().getOrCreateIdentity(providerId, kudos.getReceiverId(), true);
      if (owner == null) {
        LOG.warn("Can't find receiver identity with type/id", kudos.getReceiverType(), kudos.getReceiverId());
      } else {
        activityStorage.saveActivity(owner, activity);
        kudosService.saveKudosActivity(kudos.getTechnicalId(), getActivityId(activity.getId()));
      }
    }
  }

  private ExoSocialActivity createActivity(Kudos kudos, String parentCommentId) {
    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setType(KUDOS_ACTIVITY_COMMENT_TYPE);
    activity.setTitle("Kudos to " + kudos.getReceiverFullName());
    activity.setUserId(kudos.getSenderIdentityId());
    activity.setParentCommentId(parentCommentId);

    String senderLink = "<a href='" + kudos.getSenderURL() + "'>" + kudos.getSenderFullName() + "</a>";
    senderLink = StringEscapeUtils.unescapeHtml(senderLink);
    String receiverLink = "<a href='" + kudos.getReceiverURL() + "'>" + kudos.getReceiverFullName() + "</a>";
    receiverLink = StringEscapeUtils.unescapeHtml(receiverLink);
    String message = StringUtils.isBlank(kudos.getMessage()) ? "." : ": " + StringEscapeUtils.escapeHtml(kudos.getMessage());

    I18NActivityUtils.addResourceKeyToProcess(activity, KUDOS_ACTIVITY_COMMENT_TITLE_ID);
    I18NActivityUtils.addResourceKey(activity, KUDOS_ACTIVITY_COMMENT_TITLE_ID, senderLink, receiverLink, message, KUDOS_ICON);
    return activity;
  }

  private Long getActivityId(String commentId) {
    return (commentId == null || commentId.trim().isEmpty()) ? null
                                                             : Long.valueOf(commentId.replace(ACTIVITY_COMMENT_ID_PREFIX, ""));
  }

}
