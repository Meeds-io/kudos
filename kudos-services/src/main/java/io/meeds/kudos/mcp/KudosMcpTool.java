/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.kudos.mcp;

import static io.meeds.mcp.server.util.McpToolUtils.formatDate;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.kudos.mcp.model.KudosAllowanceModel;
import io.meeds.kudos.mcp.model.KudosResultModel;
import io.meeds.kudos.model.AccountSettings;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.model.KudosList;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.model.KudosPeriodType;
import io.meeds.kudos.model.exception.KudosAlreadyLinkedException;
import io.meeds.kudos.service.KudosService;
import io.meeds.kudos.service.utils.Utils;
import io.meeds.mcp.server.plugin.McpToolPlugin;
import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;
import io.meeds.social.activity.plugin.ActivityPermanentLinkPlugin;

/**
 * MCP tools exposing the Kudos add-on to the AI agent (EVA). Every method acts
 * as the current user, so allowance, self-send bans and space rights
 * are enforced by {@link KudosService}.
 */
@Service
@Profile("mcp-server")
public class KudosMcpTool implements McpToolPlugin {

  private static final String  USER_RECEIVER_TYPE  = "user";

  private static final String  SPACE_RECEIVER_TYPE = "space";

  private static final int     DEFAULT_LIST_LIMIT  = 10;

  private static final int     MAX_LIST_LIMIT      = 50;

  private final KudosService   kudosService;

  private final SpaceService   spaceService;

  private final IdentityManager identityManager;

  private final ActivityManager activityManager;

  private final PermanentLinkService permanentLinkService;

  public KudosMcpTool(KudosService kudosService,
                      SpaceService spaceService,
                      IdentityManager identityManager,
                      ActivityManager activityManager,
                      PermanentLinkService permanentLinkService) {
    this.kudosService = kudosService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
    this.permanentLinkService = permanentLinkService;
  }

  /**
   * Sends a kudos from the current user to a user or a space, optionally
   * published in a given space's activity stream. ObjectNotFoundException is
   * not declared: KudosService.createKudos is @SneakyThrows and declares only
   * IllegalAccessException, so the compiler cannot see the one it raises when
   * the target space disappears between its resolution here and the write. It
   * still propagates at runtime and still maps to a 404.
   */
  public KudosResultModel sendKudos(String receiverType,
                                    String receiverId,
                                    String message,
                                    String spacePrettyName) throws IllegalAccessException {
    if (StringUtils.isBlank(message)) {
      throw new IllegalArgumentException("message is required. A kudos must carry a short appreciation message.");
    }
    if (StringUtils.isBlank(receiverId)) {
      throw new IllegalArgumentException("receiver_id is required. For a user pass the username (resolve it with"
          + " get_user_by_username); for a space pass its pretty name (resolve it with get_my_spaces).");
    }
    String type = StringUtils.trimToEmpty(receiverType).toLowerCase();
    String currentUser = getCurrentUserName();

    Kudos kudos = new Kudos();
    kudos.setSenderId(currentUser);
    kudos.setMessage(message.trim());
    Space targetSpace = null;

    if (USER_RECEIVER_TYPE.equals(type)) {
      Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId.trim());
      if (receiver == null || !receiver.isEnable() || receiver.isDeleted()) {
        throw new IllegalArgumentException("No enabled user named '" + receiverId
            + "'. Resolve the exact username with get_user_by_username before sending a kudos.");
      }
      if (StringUtils.equalsIgnoreCase(receiver.getRemoteId(), currentUser)) {
        throw new IllegalStateException("You cannot send a kudos to yourself.");
      }
      kudos.setReceiverType(USER_RECEIVER_TYPE);
      kudos.setReceiverId(receiver.getRemoteId());
      kudos.setEntityType(KudosEntityType.USER_PROFILE.name());
      kudos.setEntityId(receiver.getId());
      if (StringUtils.isNotBlank(spacePrettyName)) {
        // The kudos stays addressed to the user, but its activity is published
        // in the requested space's stream instead of the receiver's own stream
        targetSpace = requireSpace(spacePrettyName);
        kudos.setSpacePrettyName(targetSpace.getPrettyName());
      }
    } else if (SPACE_RECEIVER_TYPE.equals(type)) {
      Space space = requireSpace(receiverId);
      targetSpace = space;
      if (StringUtils.isNotBlank(spacePrettyName)
          && !StringUtils.equals(space.getId(), requireSpace(spacePrettyName).getId())) {
        throw new IllegalArgumentException("space_pretty_name must be the receiving space itself when receiver_type is"
            + " 'space'. A kudos given to a space is always published in that space.");
      }
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      kudos.setReceiverType(SPACE_RECEIVER_TYPE);
      kudos.setReceiverId(space.getPrettyName());
      kudos.setSpacePrettyName(space.getPrettyName());
      kudos.setEntityType(KudosEntityType.SPACE_PROFILE.name());
      kudos.setEntityId(spaceIdentity == null ? space.getId() : spaceIdentity.getId());
    } else {
      throw new IllegalArgumentException("receiver_type must be either 'user' or 'space'.");
    }

    try {
      Kudos created = kudosService.createKudos(kudos, currentUser);
      Utils.transformKudosMessage(created);
      return toResult(created, targetSpace);
    } catch (IllegalAccessException e) {
      throw mapSendError(e, true);
    }
  }

  /**
   * Sends a kudos from the current user on a post of a space's stream. The
   * receiver is never an argument: it is derived, at execution, from the
   * activity the way the Kudos UI does ({@code KudosIdentity.js getReceiver},
   * {@code KudosButton.vue}): the receiver of the kudos when the post is
   * itself a kudos activity, else the identity that posted it (a user, or the
   * space when the space posted it). Whether the poster is still a member of
   * the space is not checked: the post belongs to the space and its poster
   * still receives the kudos. The sender, the allowance, the refusal to
   * send to oneself (the receiver being the one derived above) and the right
   * to send in the space (seeing it is enough for a post) are checked by
   * {@link KudosService#createKudos(Kudos, String)}. ObjectNotFoundException
   * is not declared for the reason given on
   * {@link #sendKudos(String, String, String, String)}.
   *
   * @param activityId the id of the post (an activity, not a comment)
   * @param message the appreciation message
   * @return the sent kudos, with the link to the activity it generated
   * @throws IllegalAccessException never thrown as such: a refusal of the
   *           service is mapped to an {@link IllegalStateException}
   */
  public KudosResultModel sendKudosOnActivity(String activityId, String message) throws IllegalAccessException {
    if (StringUtils.isBlank(message)) {
      throw new IllegalArgumentException("message is required. A kudos must carry a short appreciation message.");
    }
    if (StringUtils.isBlank(activityId) || !StringUtils.isNumeric(activityId.trim())) {
      throw new IllegalArgumentException("activity_id is required and must be the numeric id of a post."
          + " A comment id is not accepted: kudos are sent on a post.");
    }
    String currentUser = getCurrentUserName();
    ExoSocialActivity activity = activityManager.getActivity(activityId.trim());
    if (activity == null) {
      throw new IllegalArgumentException("No activity found with id " + activityId.trim() + ".");
    }
    if (activity.isComment() || activity.getParentId() != null) {
      throw new IllegalArgumentException("Activity " + activityId.trim() + " is a comment. Kudos are sent on a post,"
          + " not on a comment.");
    }
    Space space = StringUtils.isNumeric(activity.getSpaceId()) ? spaceService.getSpaceById(activity.getSpaceId()) : null;
    if (space == null) {
      throw new IllegalArgumentException("Activity " + activityId.trim() + " is not a post of a space stream."
          + " Kudos can only be sent on a post of a space.");
    }
    if (!activityManager.isActivityViewable(activity, getCurrentUserAclIdentity())) {
      throw new IllegalStateException("You are not allowed to view this activity, so you cannot send a kudos on it.");
    }

    Kudos kudos = new Kudos();
    kudos.setSenderId(currentUser);
    kudos.setMessage(message.trim());
    kudos.setEntityType(KudosEntityType.ACTIVITY.name());
    kudos.setEntityId(activity.getId());
    kudos.setParentEntityId("");
    kudos.setSpacePrettyName(space.getPrettyName());
    setActivityReceiver(kudos, activity);

    try {
      Kudos created = kudosService.createKudos(kudos, currentUser);
      Utils.transformKudosMessage(created);
      return toResult(created, space);
    } catch (IllegalAccessException e) {
      throw mapSendError(e, false);
    }
  }

  /**
   * Cancels (deletes) a kudos previously sent by the current user.
   */
  public String cancelKudos(Long kudosId) throws ObjectNotFoundException {
    if (kudosId == null || kudosId <= 0) {
      throw new IllegalArgumentException("kudos_id is required and must be a positive number.");
    }
    String currentUser = getCurrentUserName();
    try {
      kudosService.deleteKudosById(kudosId, currentUser);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("You can only cancel a kudos that you sent yourself.");
    } catch (ObjectNotFoundException e) {
      throw new ObjectNotFoundException("No kudos found with id " + kudosId + ". It may have already been cancelled.");
    } catch (KudosAlreadyLinkedException e) {
      throw new IllegalStateException("This kudos can no longer be cancelled because other kudos are already linked to it.");
    }
    return "Kudos " + kudosId + " has been cancelled.";
  }

  /**
   * Returns the current user's remaining kudos allowance for the running
   * period.
   */
  public KudosAllowanceModel getMyKudosAllowance() {
    String currentUser = getCurrentUserName();
    AccountSettings settings = kudosService.getAccountSettings(currentUser);
    KudosPeriod period = kudosService.getCurrentKudosPeriod();
    KudosPeriodType periodType = kudosService.getDefaultKudosPeriodType();
    return new KudosAllowanceModel(settings.getRemainingKudos(),
                                   periodType.name(),
                                   formatSeconds(period.getStartDateInSeconds()),
                                   formatSeconds(period.getEndDateInSeconds()));
  }

  /**
   * Lists the kudos received by a user or space during the current period.
   * Defaults to the current user when no identity id is provided.
   */
  public KudosList listReceivedKudos(Long identityId, String periodType, Integer limit) {
    long targetId = (identityId != null && identityId > 0) ? identityId : currentUserIdentityId();
    KudosPeriodType type = resolvePeriodType(periodType);
    KudosPeriod period = type.getPeriodOfTime(LocalDateTime.now());
    int max = clampLimit(limit);
    long size = kudosService.countKudosByPeriodAndReceiver(targetId,
                                                           period.getStartDateInSeconds(),
                                                           period.getEndDateInSeconds());
    KudosList kudosList = new KudosList();
    kudosList.setLimit(max);
    kudosList.setSize(size);
    if (size == 0) {
      kudosList.setKudos(List.of());
      return kudosList;
    }
    List<Kudos> kudos = kudosService.getKudosByPeriodAndReceiver(targetId,
                                                                 period.getStartDateInSeconds(),
                                                                 period.getEndDateInSeconds(),
                                                                 max);
    kudos.forEach(Utils::transformKudosMessage);
    kudosList.setKudos(kudos);
    return kudosList;
  }

  /**
   * Lists the kudos sent by the current user during the current period. Sent
   * history defaults to the current user and should not be used to enumerate
   * other users.
   */
  public KudosList listSentKudos(Long identityId, String periodType, Integer limit) {
    long targetId = (identityId != null && identityId > 0) ? identityId : currentUserIdentityId();
    KudosPeriodType type = resolvePeriodType(periodType);
    KudosPeriod period = type.getPeriodOfTime(LocalDateTime.now());
    int max = clampLimit(limit);
    long size = kudosService.countKudosByPeriodAndSender(targetId,
                                                         period.getStartDateInSeconds(),
                                                         period.getEndDateInSeconds());
    KudosList kudosList = new KudosList();
    kudosList.setLimit(max);
    kudosList.setSize(size);
    if (size == 0) {
      kudosList.setKudos(List.of());
      return kudosList;
    }
    List<Kudos> kudos = kudosService.getKudosByPeriodAndSender(targetId,
                                                               period.getStartDateInSeconds(),
                                                               period.getEndDateInSeconds(),
                                                               max);
    kudos.forEach(Utils::transformKudosMessage);
    kudosList.setKudos(kudos);
    return kudosList;
  }

  /**
   * Lists the kudos attached to an activity (or its comments), enforcing the
   * current user's right to view that activity.
   */
  public List<Kudos> listKudosOnActivity(String activityId) throws IllegalAccessException {
    if (StringUtils.isBlank(activityId)) {
      throw new IllegalArgumentException("activity_id is required.");
    }
    try {
      List<Kudos> kudos = kudosService.getKudosListOfActivity(activityId.trim(), getCurrentUserAclIdentity());
      kudos.forEach(Utils::transformKudosMessage);
      return kudos;
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("You are not allowed to view kudos on this activity."
          + " You must be able to view the activity (e.g. be a member of its space).");
    }
  }

  /**
   * Sets the receiver of a kudos sent on a post, the way the Kudos UI derives
   * it: the receiver of the kudos when the post is itself a kudos activity,
   * else the identity that posted it, as a space when the space posted it.
   *
   * @param kudos the kudos under construction
   * @param activity the targeted post
   * @throws IllegalStateException when the current user can't view the kudos
   *           the post stands for
   * @throws IllegalArgumentException when the receiver can't be determined or
   *           can't receive a kudos
   */
  private void setActivityReceiver(Kudos kudos, ExoSocialActivity activity) {
    if (StringUtils.equals(Utils.KUDOS_ACTIVITY_COMMENT_TYPE, activity.getType())) {
      Kudos targetKudos;
      try {
        targetKudos = kudosService.getKudosByActivityId(Long.parseLong(activity.getId()), getCurrentUserAclIdentity());
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("You are not allowed to view this kudos, so you cannot send a kudos on it.");
      }
      if (targetKudos == null || StringUtils.isBlank(targetKudos.getReceiverId())
          || StringUtils.isBlank(targetKudos.getReceiverType())) {
        throw new IllegalArgumentException("The kudos behind activity " + activity.getId()
            + " could not be found, so its receiver is unknown.");
      }
      kudos.setReceiverType(targetKudos.getReceiverType());
      kudos.setReceiverId(targetKudos.getReceiverId());
      return;
    }
    Identity poster = StringUtils.isBlank(activity.getPosterId()) ? null : identityManager.getIdentity(activity.getPosterId());
    if (poster == null || StringUtils.isBlank(poster.getRemoteId())) {
      throw new IllegalArgumentException("The author of activity " + activity.getId() + " could not be found.");
    }
    if (poster.isSpace()) {
      kudos.setReceiverType(SPACE_RECEIVER_TYPE);
    } else if (poster.isUser()) {
      kudos.setReceiverType(USER_RECEIVER_TYPE);
    } else {
      throw new IllegalArgumentException("The author of activity " + activity.getId() + " can't receive a kudos.");
    }
    kudos.setReceiverId(poster.getRemoteId());
  }

  /**
   * Maps a refusal of the service to a message the agent can act on.
   *
   * @param e the refusal of {@link KudosService}
   * @param redactorRequired true when sending needs the right to post in the
   *          space (a kudos published in a space stream), false when seeing
   *          the space is enough (a kudos on a post)
   * @return the exception to throw
   */
  private IllegalStateException mapSendError(IllegalAccessException e, boolean redactorRequired) {
    String msg = StringUtils.trimToEmpty(e.getMessage());
    if (msg.contains("himseld") || msg.contains("himself")) {
      return new IllegalStateException("You cannot send a kudos to yourself.");
    }
    if (msg.contains("send more kudos")) {
      return new IllegalStateException("You have no kudos left for the current period."
          + " Call get_my_kudos_allowance to see how many remain and when your allowance resets.");
    }
    if (msg.contains("redact on space") && !redactorRequired) {
      return new IllegalStateException("You are not allowed to send kudos in that space: you must be able to see it.");
    }
    if (msg.contains("redact on space")) {
      return new IllegalStateException("You are not allowed to post kudos in that space."
          + " You must be a space member who is allowed to post (a redactor).");
    }
    if (msg.contains("Target space")) {
      return new IllegalStateException("That space cannot receive this kudos. Check the space with get_my_spaces.");
    }
    return new IllegalStateException("Your kudos could not be sent: " + msg);
  }

  private Space requireSpace(String id) {
    Space space = resolveSpace(StringUtils.trimToEmpty(id));
    if (space == null) {
      throw new IllegalArgumentException("No space found matching '" + id
          + "'. Resolve the space pretty name with get_my_spaces before sending a kudos.");
    }
    return space;
  }

  private Space resolveSpace(String id) {
    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null && StringUtils.isNumeric(id)) {
      space = spaceService.getSpaceById(id);
    }
    if (space == null) {
      space = spaceService.getSpaceByGroupId(id);
    }
    if (space == null) {
      space = spaceService.getSpaceByGroupId("/spaces/" + id);
    }
    return space;
  }

  private long currentUserIdentityId() {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentUserName());
    if (identity == null) {
      throw new IllegalStateException("Could not resolve the current user's identity.");
    }
    return Long.parseLong(identity.getId());
  }

  private KudosPeriodType resolvePeriodType(String periodType) {
    if (StringUtils.isBlank(periodType)) {
      return kudosService.getDefaultKudosPeriodType();
    }
    try {
      return KudosPeriodType.valueOf(periodType.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid period_type '" + periodType
          + "'. Allowed values are: WEEK, MONTH, QUARTER, SEMESTER, YEAR.");
    }
  }

  private int clampLimit(Integer limit) {
    if (limit == null || limit <= 0) {
      return DEFAULT_LIST_LIMIT;
    }
    return Math.min(limit, MAX_LIST_LIMIT);
  }

  /**
   * Maps a sent kudos to the model returned to the agent.
   *
   * @param kudos the kudos, as read back after its creation
   * @param space the space its activity was published in, null for the
   *          receiver's own stream
   * @return the result model
   */
  private KudosResultModel toResult(Kudos kudos, Space space) {
    return new KudosResultModel(kudos.getTechnicalId(),
                                kudos.getSenderId(),
                                kudos.getReceiverId(),
                                kudos.getReceiverType(),
                                kudos.getMessage(),
                                formatSeconds(kudos.getTimeInSeconds()),
                                space == null ? null : space.getPrettyName(),
                                space == null ? null : space.getDisplayName(),
                                getActivityUrl(kudos));
  }

  /**
   * Builds the absolute link to the activity a sent kudos generated, the way
   * the platform links any activity: its permanent link, resolved by the
   * activity permanent-link plugin.
   *
   * @param kudos the sent kudos
   * @return the activity's absolute URL, or null when the kudos has no
   *         generated activity or its link can't be resolved
   */
  private String getActivityUrl(Kudos kudos) {
    if (kudos.getActivityId() <= 0) {
      return null;
    }
    try {
      return CommonsUtils.getCurrentDomain()
          + permanentLinkService.getLink(new PermanentLinkObject(ActivityPermanentLinkPlugin.OBJECT_TYPE,
                                                                 String.valueOf(kudos.getActivityId())));
    } catch (ObjectNotFoundException e) {
      return null;
    }
  }

  private String formatSeconds(long seconds) {
    return seconds <= 0 ? null : formatDate(seconds * 1000);
  }

}
