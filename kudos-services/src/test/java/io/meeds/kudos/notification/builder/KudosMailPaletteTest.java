/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.notification.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.mail.MailPalette;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.TimeConvertUtils;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.social.common.xmlprocessor.XMLProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import groovy.text.GStringTemplateEngine;

/**
 * The kudos mail is written on the recipient's palette: the template compiles
 * with the engine of the mail channel, keeps no palette colour as a literal
 * and prints the bound palette, and the builder writes the kudos message and
 * the activity it answers, user content, for the recipient's display mode.
 * The template lives in the <code>kudos-webapps</code> module and is read
 * from the source tree of the reactor.
 */
public class KudosMailPaletteTest {

  private static final Path    TEMPLATE        =
                                        Paths.get("../kudos-webapps/src/main/webapp/WEB-INF/conf/kudos/templates/notification/mail/KudosReceiverMailPlugin.gtmpl");

  // the light literals the palette replaces; the footer label's link colour is replaced where the label is printed
  private static final Pattern PALETTE_LITERAL = Pattern.compile("(?i)(bgcolor=\"|background-color:\\s*)#(ffffff|f9f9f9|f1f1f1)"
      + "|(?<![a-z-])(?<!replace\\(\"color: )color:\\s*#(333333|999999)|(?<!replace\\(\"color: )#2f5e92|border[a-z-]*:\\s*\\d+px solid\\s*#(d8d8d8|224886)");

  @Test
  void theTemplateCompilesAndReadsThePalette() throws Exception {
    String text = Files.readString(TEMPLATE, StandardCharsets.UTF_8);
    new GStringTemplateEngine().createTemplate(text);
    assertFalse(PALETTE_LITERAL.matcher(text).find(), "a palette colour is left as a literal");
    assertTrue(text.contains("MailPalette.of(_templateContext)"));
  }

  @Test
  void theBoundPaletteReachesTheRenderedMail() throws Exception {
    Map<String, String> palette = new HashMap<>();
    palette.put("surface", "#1E2328");
    palette.put("background", "#14181B");
    palette.put("text", "#E6EAEE");
    palette.put("secondaryText", "#9AA4AD");
    palette.put("link", "#5FA9AC");
    palette.put("border", "#3A4148");
    palette.put("primary", "#3F8487");
    Map<String, Object> templateContext = new HashMap<>();
    templateContext.put(MailPalette.CONTEXT_KEY, palette);
    StringWriter writer = new StringWriter();
    Map<String, Object> binding = new HashMap<>();
    binding.put("_ctx", new TemplateContextStub());
    binding.put("_templateContext", templateContext);
    binding.put("_writer", writer);
    for (String name : new String[] { "ACTIVITY", "KUDOS_MESSAGE", "VIEW_FULL_DISCUSSION_ACTION_URL", "FIRSTNAME",
        "PROFILE_URL", "SPACE", "SPACE_URL", "USER", "AVATAR", "FOOTER_LINK", "SUBJECT" }) {
      binding.put(name, name.toLowerCase());
    }
    binding.put("IS_SPACE_RECEIVER", "false");
    new GStringTemplateEngine().createTemplate(Files.readString(TEMPLATE, StandardCharsets.UTF_8)).make(binding).writeTo(writer);
    String html = writer.toString();
    assertTrue(html.contains("bgcolor=\"#1E2328\""), html);
    assertTrue(html.contains("color:#E6EAEE") || html.contains("color: #E6EAEE"), html);
    assertFalse(html.contains("bgcolor=\"#ffffff\""), html);
  }

  @Test
  void theMessageAndTheActivityAreWrittenForARecipientInDark() {
    TemplateContext templateContext = buildMail("dark");
    assertEquals("dark", MailPalette.of(templateContext).get(MailPalette.MODE));
    assertEquals("<p>thanks</p>", templateContext.get("KUDOS_MESSAGE"));
    assertEquals("<p>the post</p>", templateContext.get("ACTIVITY"));
  }

  @Test
  void theMessageAndTheActivityAreUntouchedForALightRecipient() {
    TemplateContext templateContext = buildMail("light");
    assertEquals("<p style=\"color:#000000\">thanks</p>", templateContext.get("KUDOS_MESSAGE"));
    assertEquals("<p style=\"color:#000000\">the post</p>", templateContext.get("ACTIVITY"));
  }

  private TemplateContext buildMail(String mode) {
    BrandingService brandingService = mock(BrandingService.class);
    when(brandingService.getDisplayMode("mary")).thenReturn(mode);
    when(brandingService.getMailPalette("dark".equals(mode))).thenReturn(new HashMap<>(Map.of("surface", "#1E2328")));
    IdentityManager identityManager = mock(IdentityManager.class);
    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identityManager.getOrCreateIdentity(anyString(), anyString(), anyBoolean())).thenReturn(identity);
    when(identityManager.getOrCreateIdentity(anyString(), anyString())).thenReturn(identity);
    when(identity.getProfile()).thenReturn(profile);
    when(identity.getRemoteId()).thenReturn("john");
    when(profile.getFullName()).thenReturn("John Doe");
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn("Mary");
    ActivityManager activityManager = mock(ActivityManager.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activity.getId()).thenReturn("12");
    when(activity.getTitle()).thenReturn("title");
    when(activity.getParentId()).thenReturn("10");
    when(activityManager.getActivity("12")).thenReturn(activity);
    when(activityManager.getActivityTitle("10")).thenReturn("<p style=\"color:#000000\">the post</p>");
    XMLProcessor xmlProcessor = mock(XMLProcessor.class);
    when(xmlProcessor.process(anyString())).thenAnswer(call -> call.getArgument(0));
    TemplateProvider templateProvider = mock(TemplateProvider.class);
    when(templateProvider.getChannelKey()).thenReturn(ChannelKey.key("MAIL_CHANNEL"));

    NotificationInfo notification = NotificationInfo.instance().setTo("mary").key("KudosReceiverPlugin");
    notification.setLastModifiedDate(System.currentTimeMillis());
    notification.with("SENDER_ID", "john")
                .with("KUDOS_MESSAGE", "<p style=\"color:#000000\">thanks</p>")
                .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), "12");
    NotificationContext ctx = mock(NotificationContext.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    AtomicReference<TemplateContext> rendered = new AtomicReference<>();

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class);
         MockedStatic<Utils> utils = mockStatic(Utils.class);
         MockedStatic<LinkProviderUtils> links = mockStatic(LinkProviderUtils.class);
         MockedStatic<NotificationPluginUtils> pluginUtils = mockStatic(NotificationPluginUtils.class);
         MockedStatic<MentionUtils> mentionUtils = mockStatic(MentionUtils.class);
         MockedStatic<TimeConvertUtils> timeUtils = mockStatic(TimeConvertUtils.class);
         MockedStatic<TemplateUtils> templateUtils = mockStatic(TemplateUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(BrandingService.class)).thenReturn(brandingService);
      commonsUtils.when(CommonsUtils::getCurrentDomain).thenReturn("http://host");
      utils.when(Utils::getIdentityManager).thenReturn(identityManager);
      utils.when(Utils::getActivityManager).thenReturn(activityManager);
      links.when(() -> LinkProviderUtils.getRedirectUrl(anyString(), anyString())).thenReturn("/redirect");
      links.when(LinkProviderUtils::getBaseUrl).thenReturn("http://host");
      pluginUtils.when(() -> NotificationPluginUtils.getLanguage("mary")).thenReturn("en");
      mentionUtils.when(() -> MentionUtils.substituteRoleWithLocale(anyString(), any(Locale.class)))
                  .thenAnswer(call -> call.getArgument(0));
      timeUtils.when(() -> TimeConvertUtils.convertXTimeAgoByTimeServer(any(), anyString(), any(Locale.class), anyInt()))
               .thenReturn("now");
      templateUtils.when(() -> TemplateUtils.processSubject(any(TemplateContext.class))).thenReturn("subject");
      templateUtils.when(() -> TemplateUtils.processGroovy(any(TemplateContext.class))).thenAnswer(call -> {
        rendered.set(call.getArgument(0));
        return "";
      });

      MessageInfo message = new KudosTemplateBuilder(templateProvider, false, xmlProcessor).buildMessage(ctx);
      assertNotNull(message);
      assertNull(ctx.getException());
    }
    return rendered.get();
  }

  /** Stands for the template context the engine gives a template. */
  public static class TemplateContextStub {

    public String appRes(String key, Object... args) {
      return "[" + key + "]";
    }

    public String escapeHTML(String text) {
      return text;
    }

    public void include(String path, Object templateContext) {
      // the shared header and footer are pinned by the commons module
    }
  }

}
