package org.exoplatform.kudos.service;

import static org.exoplatform.kudos.service.utils.Utils.*;

import java.util.*;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.kudos.dao.KudosDAO;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;

public class KudosStorage {
  private static final Log LOG = ExoLogger.getLogger(KudosStorage.class);

  private KudosDAO         kudosDAO;

  private IdentityManager  identityManager;

  public KudosStorage(KudosDAO kudosDAO) {
    this.kudosDAO = kudosDAO;
  }

  public Kudos getKudoById(long id) {
    KudosEntity kudosEntity = kudosDAO.find(id);
    if (kudosEntity == null) {
      LOG.warn("Can't find Kudos with id {}", id);
      return null;
    } else {
      return fromEntity(kudosEntity);
    }
  }

  public Kudos createKudos(Kudos kudos) {
    KudosEntity kudosEntity = toNewEntity(kudos);
    kudosEntity = kudosDAO.create(kudosEntity);
    return fromEntity(kudosEntity);
  }

  public void saveKudosActivityId(long kudosId, long activityId) {
    KudosEntity kudosEntity = kudosDAO.find(kudosId);
    if (kudosEntity == null) {
      throw new IllegalStateException("Can't find Kudos with id " + kudosId);
    } else {
      kudosEntity.setActivityId(activityId);
      kudosDAO.update(kudosEntity);
    }
  }

  public List<Kudos> getKudosByPeriod(KudosPeriod kudosPeriod, int limit) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriod(kudosPeriod, limit);
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
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByEntity(KudosEntityType.valueOf(entityType).ordinal(),
                                                                Long.parseLong(entityId),
                                                                limit);
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
    return kudosDAO.countKudosByEntity(KudosEntityType.valueOf(entityType).ordinal(), Long.parseLong(entityId));
  }
  
  public long countKudosByEntityAndSender(String entityType, String entityId, String senderIdentityId) {
    return kudosDAO.countKudosByEntityAndSender(KudosEntityType.valueOf(entityType).ordinal(), Long.parseLong(entityId), Long.parseLong(senderIdentityId));
  }

  public long countKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    Identity identity = getIdentityManager().getOrCreateIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                                : SpaceIdentityProvider.NAME,
                                                                 receiverId);
    return kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod,
                                                  Long.parseLong(identity.getId()),
                                                  isReceiverUser);
  }

  public List<Kudos> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId, int limit) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    long identityId = getIdentityId(receiverId, isReceiverUser);
    if (identityId <= 0) {
      return Collections.emptyList();
    }
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod,
                                                                           identityId,
                                                                           isReceiverUser,
                                                                           limit);
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
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, senderIdentityId, limit);
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
    return kudosDAO.countKudosByPeriodAndSender(kudosPeriod, senderIdentityId);
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

  public KudosEntity getKudosByActivityId(Long  activityId){
    return kudosDAO.getKudosByActivityId(activityId);
  }
}
