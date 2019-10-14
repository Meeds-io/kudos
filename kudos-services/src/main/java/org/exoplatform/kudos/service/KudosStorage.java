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

/**
 * This is a placeholder class to manage Kudos storage in RDBMS
 */
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

  public List<Kudos> getAllKudosByPeriod(KudosPeriod kudosPeriod) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByPeriod(kudosPeriod);
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public List<Kudos> getAllKudosByPeriodAndEntityType(KudosPeriod kudosPeriod, String entityType) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByPeriodAndEntityType(kudosPeriod,
                                                                                KudosEntityType.valueOf(entityType).ordinal());
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public List<Kudos> getAllKudosByEntity(String entityType, String entityId) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByEntity(KudosEntityType.valueOf(entityType).ordinal(),
                                                                   Long.parseLong(entityId));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public long countKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    Identity identity = getIdentityManager().getOrCreateIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                                : SpaceIdentityProvider.NAME,
                                                                 receiverId,
                                                                 true);
    return kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod,
                                                  Long.parseLong(identity.getId()),
                                                  isReceiverUser);
  }

  public List<Kudos> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId) {
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    long identityId = 0;
    if (isReceiverUser) {
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
      if (identity == null) {
        return Collections.emptyList();
      }
      identityId = Long.parseLong(identity.getId());
    } else {
      Space space = getSpace(receiverId);
      if (space == null) {
        return Collections.emptyList();
      }
      identityId = Long.parseLong(space.getId());
    }
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod,
                                                                           identityId,
                                                                           isReceiverUser);
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

  public List<Kudos> getKudosByPeriodAndSender(KudosPeriod kudosPeriod, String senderId) {
    List<Kudos> kudosList = new ArrayList<>();
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriodAndSender(kudosPeriod, Long.parseLong(identity.getId()));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public long countKudosByPeriodAndSender(KudosPeriod kudosPeriod, String senderId) {
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    return kudosDAO.countKudosByPeriodAndSender(kudosPeriod, Long.parseLong(identity.getId()));
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }
}
