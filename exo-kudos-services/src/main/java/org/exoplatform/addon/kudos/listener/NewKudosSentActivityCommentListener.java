package org.exoplatform.addon.kudos.listener;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.notification.LinkProviderUtils;

/**
 * A listener to add comment on activity
 */
public class NewKudosSentActivityCommentListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(NewKudosSentActivityCommentListener.class);

  private ActivityStorage  activityStorage;

  private IdentityManager  identityManager;

  public NewKudosSentActivityCommentListener(ActivityStorage activityStorage, IdentityManager identityManager) {
    this.activityStorage = activityStorage;
    this.identityManager = identityManager;
  }

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    if (KudosEntityType.valueOf(kudos.getEntityType()) != KudosEntityType.ACTIVITY) {
      return;
    }
    String activityId = kudos.getEntityId();
    try {
      ExoSocialActivity activity = this.activityStorage.getActivity(activityId);
      if (activity == null) {
        throw new IllegalStateException("Activity with id '" + activityId + "' wasn't found");
      }
      ExoSocialActivity activityComment = createComment(kudos.getSenderId(),
                                                        kudos.getReceiverType(),
                                                        kudos.getReceiverId(),
                                                        kudos.getMessage());
      activityStorage.saveComment(activity, activityComment);
    } catch (Exception e) {
      LOG.warn("Error adding comment on activity with id '" + activityId + "' for Kudos with id " + kudos.getTechnicalId(), e);
    }
  }

  private ExoSocialActivity createComment(String senderId, String receiverType, String receiverId, String message) {
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle("");
    comment.setType(KUDOS_ACTIVITY_COMMENT_TYPE);
    comment.setTitleId(KUDOS_ACTIVITY_COMMENT_TITLE_ID);
    Identity senderIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    comment.setUserId(senderIdentity.getId());

    String senderURL = LinkProviderUtils.getRedirectUrl("user", senderId);
    String senderLink = "<a href='" + senderURL + "'>" + senderIdentity.getProfile().getFullName() + "</a>";
    senderLink = StringEscapeUtils.unescapeHtml(senderLink);

    receiverType = getReceiverType(receiverType);
    String receiverProviderId = getReceiverIdentityProviderType(receiverType);
    boolean isReceiverUser = OrganizationIdentityProvider.NAME.equals(receiverProviderId);
    String fullName = receiverId;
    String receiverURLId = receiverId;
    if (isReceiverUser) {
      Identity receiverIdentity = identityManager.getOrCreateIdentity(receiverProviderId, receiverId, true);
      if (receiverIdentity == null) {
        LOG.warn("Can't find user with username : {}", receiverId);
      } else {
        fullName = receiverIdentity.getProfile().getFullName();
      }
    } else {
      Space space = getSpace(receiverId);
      if (space == null) {
        LOG.warn("Can't find space with pretty name : {}", receiverId);
      } else {
        fullName = space.getDisplayName();
        receiverURLId = space.getId();
      }
    }
    String receiverURL = LinkProviderUtils.getRedirectUrl(receiverType, receiverURLId);
    String receiverLink = StringEscapeUtils.unescapeHtml("<a href='" + receiverURL + "'>" + fullName + "</a>");

    message =
            StringUtils.isBlank(message) ? "."
                                         : StringEscapeUtils.escapeHtml(": " + message.replace(RESOURCE_BUNDLE_VALUES_CHARACTER,
                                                                                               RESOURCE_BUNDLE_ESCAPE_CHARACTER));

    try {
      message = HTMLSanitizer.sanitize(message);
    } catch (Exception e) {
      LOG.warn("Error while sanitizing message", message);
    }
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(RESOURCE_BUNDLE_VALUES_PARAM,
                       senderLink + RESOURCE_BUNDLE_VALUES_CHARACTER + receiverLink + RESOURCE_BUNDLE_VALUES_CHARACTER + message);
    comment.setTemplateParams(templateParams);
    return comment;
  }

}
