package org.exoplatform.kudos.notification.builder;

import static org.exoplatform.kudos.service.utils.Utils.*;

import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.xmlprocessor.XMLProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class KudosTemplateBuilder extends AbstractTemplateBuilder {

  private static final String ACTIVITY_PARAM = "ACTIVITY";

  private TemplateProvider templateProvider;

  private boolean          pushNotification;

  private XMLProcessor     xmlProcessor;

  public KudosTemplateBuilder(TemplateProvider templateProvider, boolean pushNotification , XMLProcessor xmlProcessor) {
    this.templateProvider = templateProvider;
    this.pushNotification = pushNotification;
    this.xmlProcessor = xmlProcessor;
  }

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) { // NOSONAR
    NotificationInfo notification = ctx.getNotificationInfo();
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = null;
    if (StringUtils.isNotBlank(activityId) && !StringUtils.equals(activityId, "0")) {
      activity = Utils.getActivityManager().getActivity(ACTIVITY_COMMENT_ID_PREFIX + activityId);
      if (activity == null) {
        activity = Utils.getActivityManager().getActivity(activityId);
      }
    }

    String pluginId = notification.getKey().getId();
    String language = getLanguage(notification);
    TemplateContext templateContext =
                                    TemplateContext.newChannelInstance(this.templateProvider.getChannelKey(), pluginId, language);

    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(notification.getLastModifiedDate());
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME",
                        TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(),
                                                                     "EE, dd yyyy",
                                                                     Locale.of(language),
                                                                     TimeConvertUtils.YEAR));

    Identity senderIdentity = Utils.getIdentityManager()
                                   .getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                        notification.getValueOwnerParameter("SENDER_ID"));
    String receiverType = notification.getValueOwnerParameter("RECEIVER_TYPE");
    if (SPACE_ACCOUNT_TYPE.equals(receiverType)) {
      String receiverId = notification.getValueOwnerParameter("RECEIVER_ID");
      Space space = getSpace(receiverId);
      if (space != null) {
        templateContext.put("SPACE", space.getDisplayName());
        templateContext.put("SPACE_URL", CommonsUtils.getCurrentDomain() + LinkProvider.getSpaceUri(space.getPrettyName()));
      }
      templateContext.put("IS_SPACE_RECEIVER", "true");
    } else {
      templateContext.put("IS_SPACE_RECEIVER", "false");
    }

    templateContext.put("USER", senderIdentity.getProfile().getFullName());
    templateContext.put("AVATAR",
                        senderIdentity.getProfile().getAvatarUrl() != null ? senderIdentity.getProfile().getAvatarUrl()
                                                                           : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
    templateContext.put("KUDOS_ID", notification.getValueOwnerParameter("KUDOS_ID"));
    String message = notification.getValueOwnerParameter("KUDOS_MESSAGE");
    message = (String)xmlProcessor.process(message);
    if (StringUtils.isBlank(message)) {
      message = "";
    }
    templateContext.put("KUDOS_MESSAGE", message);
    String title = "";
    String notificationURL = CommonsUtils.getCurrentDomain();

    String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
    if (activity != null) {
      title = " : " + SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
      if (activity.isComment() && StringUtils.isNotBlank(activity.getParentCommentId())) {
        notificationURL = LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment_reply",
                                                           activity.getParentId() + "-" + activity.getParentCommentId() + "-"
                                                               + activity.getId());
      } else if (activity.isComment()) {
        notificationURL = LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment",
                                                           activity.getParentId() + "-" + activity.getId());
      } else {
        notificationURL = LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId());
      }
    }

    templateContext.put("SUBJECT", title);
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", senderIdentity.getRemoteId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", notificationURL);

    MessageInfo messageInfo = new MessageInfo();
    if (pushNotification) {
      messageInfo.subject(notificationURL);
    } else {
      messageInfo.subject(TemplateUtils.processSubject(templateContext));
    }
    // body construction must be made after subject building
    if (activity != null) {
      if (StringUtils.isNotBlank(activity.getParentCommentId())) {
        String parentCommentTitle = getActivityTitle(activity.getParentCommentId(), language);
        templateContext.put(ACTIVITY_PARAM, parentCommentTitle);
      } else if (StringUtils.isNotBlank(activity.getParentId())) {
        String parentActivityTitle = getActivityTitle(activity.getParentId(), language);
        templateContext.put(ACTIVITY_PARAM, parentActivityTitle);
      } else {
        templateContext.put(ACTIVITY_PARAM, "");
      }
      messageInfo.body(TemplateUtils.processGroovy(templateContext));
    } else {
      templateContext.put(ACTIVITY_PARAM, "");
      messageInfo.body(TemplateUtils.processGroovy(templateContext));
    }
    ctx.setException(templateContext.getException());
    return messageInfo.end();
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

  protected ExoSocialActivity getI18N(ExoSocialActivity activity, Locale locale) {
    I18NActivityProcessor i18NActivityProcessor = PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      activity = i18NActivityProcessor.process(activity, locale);
    }
    return activity;
  }

  private String getActivityTitle(String activityId, String language) {
    return MentionUtils.substituteRoleWithLocale(Utils.getActivityManager().getActivityTitle(activityId),
                                                 Locale.forLanguageTag(language));
  }

}
