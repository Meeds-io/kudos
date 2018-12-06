package org.exoplatform.addon.kudos.listener;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * A listener to add comment on activity
 */
public class NewKudosSentActivityCommentListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(NewKudosSentActivityCommentListener.class);

  private ActivityManager  activityManager;

  private ActivityStorage  activityStorage;

  public NewKudosSentActivityCommentListener(ActivityManager activityManager, ActivityStorage activityStorage) {
    this.activityStorage = activityStorage;
    this.activityManager = activityManager;
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.ACTIVITY
        || KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
      String activityId = kudos.getEntityId();
      try {
        String parentCommentId = null;
        ExoSocialActivity activity = null;
        if (KudosEntityType.valueOf(kudos.getEntityType()) == KudosEntityType.COMMENT) {
          ExoSocialActivity comment = this.activityManager.getActivity("comment" + activityId);
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
        ExoSocialActivity activityComment = createComment(kudos, parentCommentId);
        activityStorage.saveComment(activity, activityComment);
      } catch (Exception e) {
        LOG.warn("Error adding comment on activity with id '" + activityId + "' for Kudos with id " + kudos.getTechnicalId(), e);
      }
    }
  }

  private ExoSocialActivity createComment(Kudos kudos, String parentCommentId) {
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle("");
    comment.setType(KUDOS_ACTIVITY_COMMENT_TYPE);
    comment.setTitleId(KUDOS_ACTIVITY_COMMENT_TITLE_ID);
    comment.setUserId(kudos.getSenderIdentityId());
    comment.setParentCommentId(parentCommentId);

    String senderLink = "<a href='" + kudos.getSenderURL() + "'>" + kudos.getSenderFullName() + "</a>";
    senderLink = StringEscapeUtils.unescapeHtml(senderLink);
    String receiverLink = "<a href='" + kudos.getReceiverURL() + "'>" + kudos.getReceiverFullName() + "</a>";
    receiverLink = StringEscapeUtils.unescapeHtml(receiverLink);

    String message =
                   StringUtils.isBlank(kudos.getMessage()) ? "."
                                                           : ": "
                                                               + StringEscapeUtils.escapeHtml(kudos.getMessage())
                                                                                  .replace(RESOURCE_BUNDLE_VALUES_CHARACTER,
                                                                                           RESOURCE_BUNDLE_ESCAPE_CHARACTER)
                                                                                  .replace(RESOURCE_BUNDLE_KEYS_CHARACTER,
                                                                                           RESOURCE_BUNDLE_ESCAPE_KEY_CHARACTER);

    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(RESOURCE_BUNDLE_VALUES_PARAM,
                       senderLink + RESOURCE_BUNDLE_VALUES_CHARACTER + receiverLink + RESOURCE_BUNDLE_VALUES_CHARACTER + message);
    comment.setTemplateParams(templateParams);
    return comment;
  }

}
