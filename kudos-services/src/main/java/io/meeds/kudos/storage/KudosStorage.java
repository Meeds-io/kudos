/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.storage;

import static io.meeds.kudos.service.utils.Utils.USER_ACCOUNT_TYPE;
import static io.meeds.kudos.service.utils.Utils.fromEntity;
import static io.meeds.kudos.service.utils.Utils.getSpace;
import static io.meeds.kudos.service.utils.Utils.toEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.kudos.dao.KudosDAO;
import io.meeds.kudos.entity.KudosEntity;
import io.meeds.kudos.model.Kudos;
import io.meeds.kudos.model.KudosEntityType;
import io.meeds.kudos.model.KudosPeriod;
import io.meeds.kudos.service.utils.Utils;

@Component
public class KudosStorage {

  private static final Log LOG = ExoLogger.getLogger(KudosStorage.class);

  @Autowired
  private KudosDAO         kudosDAO;

  @Autowired
  private IdentityManager  identityManager;

  public Kudos getKudoById(long id) {
    KudosEntity kudosEntity = kudosDAO.findById(id).orElse(null);
    if (kudosEntity == null) {
      LOG.warn("Can't find Kudos with id {}", id);
      return null;
    } else {
      return fromEntity(kudosEntity);
    }
  }

  public Kudos createKudos(Kudos kudos) {
    KudosEntity kudosEntity = toEntity(kudos);
    kudosEntity.setId(null);
    kudosEntity = kudosDAO.save(kudosEntity);
    return fromEntity(kudosEntity);
  }

  public void deleteKudosById(long kudosId) {
    kudosDAO.deleteById(kudosId);
  }

  public void saveKudosActivityId(long kudosId, long activityId) {
    KudosEntity kudosEntity = kudosDAO.findById(kudosId).orElse(null);
    if (kudosEntity == null) {
      throw new IllegalStateException("Can't find Kudos with id " + kudosId);
    } else {
      kudosEntity.setActivityId(activityId);
      kudosDAO.save(kudosEntity);
    }
  }

  public List<Kudos> getKudosByPeriod(KudosPeriod kudosPeriod, int limit) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.findByCreatedDateBetweenOrderByCreatedDateDesc(kudosPeriod.getStartDateInSeconds(),
                                                                                              kudosPeriod.getEndDateInSeconds(),
                                                                                              Limit.of(limit));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public List<Kudos> getKudosByEntity(String entityType, String entityId, int limit) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities =
                                    kudosDAO.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(KudosEntityType.valueOf(entityType)
                                                                                                              .ordinal(),
                                                                                               Long.parseLong(entityId),
                                                                                               Limit.of(limit));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public long countKudosByEntity(String entityType, String entityId) {
    return kudosDAO.countByEntityTypeAndEntityId(KudosEntityType.valueOf(entityType).ordinal(), Long.parseLong(entityId));
  }

  public long countKudosByEntityAndSender(String entityType, String entityId, String senderIdentityId) {
    return kudosDAO.countByEntityTypeAndEntityIdAndSenderId(KudosEntityType.valueOf(entityType).ordinal(),
                                                            Long.parseLong(entityId),
                                                            Long.parseLong(senderIdentityId));
  }

  public long countKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    Identity identity = getIdentityManager().getOrCreateIdentity(
                                                                 isReceiverUser ? OrganizationIdentityProvider.NAME :
                                                                                SpaceIdentityProvider.NAME,
                                                                 receiverId);
    return kudosDAO.countByCreatedDateBetweenAndReceiverIdAndIsReceiverUser(kudosPeriod.getStartDateInSeconds(),
                                                                            kudosPeriod.getEndDateInSeconds(),
                                                                            Long.parseLong(identity.getId()),
                                                                            isReceiverUser);
  }

  public Map<Long, Long> countKudosByPeriodAndReceivers(KudosPeriod kudosPeriod, List<Long> receiversId) {
    return kudosDAO.countByCreatedDateBetweenAndReceiverIdIn(kudosPeriod.getStartDateInSeconds(),
                                                             kudosPeriod.getEndDateInSeconds(),
                                                             receiversId)
                   .stream()
                   .collect(Collectors.toMap(r -> r.getReceiverId(), r -> r.getCount()));
  }

  public List<Kudos> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId, int limit) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    long identityId = getIdentityId(receiverId, isReceiverUser);
    if (identityId <= 0) {
      return Collections.emptyList();
    }
    List<KudosEntity> kudosEntities =
                                    kudosDAO.findByCreatedDateBetweenAndReceiverIdAndIsReceiverUserOrderByCreatedDateDesc(kudosPeriod.getStartDateInSeconds(),
                                                                                                                          kudosPeriod.getEndDateInSeconds(),
                                                                                                                          identityId,
                                                                                                                          isReceiverUser,
                                                                                                                          Limit.of(limit));
    if (kudosEntities != null) {
      List<Kudos> kudosList = new ArrayList<>();
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
      return kudosList;
    }
    return Collections.emptyList();
  }

  public List<Kudos> getKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderIdentityId, int limit) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities =
                                    kudosDAO.findByCreatedDateBetweenAndSenderIdOrderByCreatedDateDesc(kudosPeriod.getStartDateInSeconds(),
                                                                                                       kudosPeriod.getEndDateInSeconds(),
                                                                                                       senderIdentityId,
                                                                                                       Limit.of(limit));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public long countKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderIdentityId) {
    return kudosDAO.countByCreatedDateBetweenAndSenderId(kudosPeriod.getStartDateInSeconds(),
                                                         kudosPeriod.getEndDateInSeconds(),
                                                         senderIdentityId);
  }

  private long getIdentityId(String remoteId, boolean isReceiverUser) {
    long identityId = 0;
    if (isReceiverUser) {
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId);
      if (identity == null) {
        return 0;
      }
      identityId = Long.parseLong(identity.getId());
    } else {
      Space space = getSpace(remoteId);
      if (space == null) {
        return 0;
      }
      identityId = Long.parseLong(space.getId());
    }
    return identityId;
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }

  public Kudos getKudosByActivityId(Long activityId) {
    KudosEntity kudosEntity = kudosDAO.findByActivityId(activityId);
    return fromEntity(kudosEntity);
  }

  public List<Kudos> getKudosListOfActivity(Long activityId) {
    List<KudosEntity> kudosEntities = kudosDAO.findKudosListOfActivity(activityId,
                                                                       Arrays.asList(KudosEntityType.ACTIVITY.ordinal(),
                                                                                     KudosEntityType.COMMENT.ordinal()));
    return CollectionUtils.isEmpty(kudosEntities) ? Collections.emptyList() :
                                                  kudosEntities.stream()
                                                               .map(Utils::fromEntity)
                                                               .toList();
  }

  public long countKudosOfActivity(Long activityId) {
    return kudosDAO.countKudosListOfActivity(activityId,
                                             Arrays.asList(KudosEntityType.ACTIVITY.ordinal(),
                                                           KudosEntityType.COMMENT.ordinal()));
  }

  public Kudos updateKudos(Kudos kudos) {
    KudosEntity kudosEntity = toEntity(kudos);
    kudosEntity = kudosDAO.save(kudosEntity);
    return fromEntity(kudosEntity);
  }

}
