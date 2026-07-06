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

/**
 * MCP tools exposing the Kudos add-on to the AI agent (EVA). Every method acts
 * as the current user, so allowance, self-send bans and space redactor rights
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

  public KudosMcpTool(KudosService kudosService,
                      SpaceService spaceService,
                      IdentityManager identityManager,
                      ActivityManager activityManager) {
    this.kudosService = kudosService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
  }

  /**
   * Sends a kudos from the current user to a user or a space.
   */
  public KudosResultModel sendKudos(String receiverType,
                                    String receiverId,
                                    String message) throws IllegalAccessException, ObjectNotFoundException {
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
    } else if (SPACE_RECEIVER_TYPE.equals(type)) {
      Space space = resolveSpace(receiverId.trim());
      if (space == null) {
        throw new IllegalArgumentException("No space found matching '" + receiverId
            + "'. Resolve the space pretty name with get_my_spaces before sending a kudos.");
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
      return toResult(created);
    } catch (IllegalAccessException e) {
      throw mapSendError(e);
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

  private IllegalStateException mapSendError(IllegalAccessException e) {
    String msg = StringUtils.trimToEmpty(e.getMessage());
    if (msg.contains("himseld") || msg.contains("himself")) {
      return new IllegalStateException("You cannot send a kudos to yourself.");
    }
    if (msg.contains("send more kudos")) {
      return new IllegalStateException("You have no kudos left for the current period."
          + " Call get_my_kudos_allowance to see how many remain and when your allowance resets.");
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

  private KudosResultModel toResult(Kudos kudos) {
    return new KudosResultModel(kudos.getTechnicalId(),
                                kudos.getSenderId(),
                                kudos.getReceiverId(),
                                kudos.getReceiverType(),
                                kudos.getMessage(),
                                formatSeconds(kudos.getTimeInSeconds()));
  }

  private String formatSeconds(long seconds) {
    return seconds <= 0 ? null : formatDate(seconds * 1000);
  }

}
