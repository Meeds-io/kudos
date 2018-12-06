package org.exoplatform.addon.kudos.notification.builder;

import static org.exoplatform.addon.kudos.service.utils.Utils.SPACE_ACCOUNT_TYPE;
import static org.exoplatform.addon.kudos.service.utils.Utils.getSpace;

import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class KudosActivityTemplateBuilder extends AbstractTemplateBuilder {

  private TemplateProvider templateProvider;

  private boolean          pushNotification;

  public KudosActivityTemplateBuilder(TemplateProvider templateProvider, boolean pushNotification) {
    this.templateProvider = templateProvider;
    this.pushNotification = pushNotification;
  }

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {

    NotificationInfo notification = ctx.getNotificationInfo();
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    if (activity == null) {
      return null;
    }

    String pluginId = notification.getKey().getId();
    String language = getLanguage(notification);
    TemplateContext templateContext =
                                    TemplateContext.newChannelInstance(this.templateProvider.getChannelKey(), pluginId, language);

    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(notification.getLastModifiedDate());
    templateContext.put("READ",
                        Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read"
                                                                                                                              : "unread");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME",
                        TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(),
                                                                     "EE, dd yyyy",
                                                                     new Locale(language),
                                                                     TimeConvertUtils.YEAR));

    Identity senderIdentity = Utils.getIdentityManager()
                                   .getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                        notification.getValueOwnerParameter("SENDER_ID"),
                                                        true);
    String receiverType = notification.getValueOwnerParameter("RECEIVER_TYPE");
    if (SPACE_ACCOUNT_TYPE.equals(receiverType) || SpaceIdentityProvider.NAME.equals(receiverType)) {
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
    if (StringUtils.isBlank(message)) {
      message = "";
    }
    templateContext.put("KUDOS_MESSAGE", StringEscapeUtils.escapeHtml(message));
    String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
    String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
    templateContext.put("SUBJECT", title);
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", senderIdentity.getRemoteId()));
    String activityURL = LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId());
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", activityURL);
    MessageInfo messageInfo = new MessageInfo();
    if (pushNotification) {
      messageInfo.subject(activityURL);
    } else {
      messageInfo.subject(TemplateUtils.processSubject(templateContext));
    }
    String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
    ctx.setException(templateContext.getException());
    messageInfo.body(body);
    return messageInfo.end();
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

  protected ExoSocialActivity getI18N(ExoSocialActivity activity, Locale locale) {
    I18NActivityProcessor i18NActivityProcessor =
                                                (I18NActivityProcessor) PortalContainer.getInstance()
                                                                                       .getComponentInstanceOfType(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      activity = i18NActivityProcessor.process(activity, locale);
    }
    return activity;
  }

};
