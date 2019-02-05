package org.exoplatform.addon.kudos.service;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.addon.kudos.dao.KudosDAO;
import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * This is a placeholder class to manage Kudos storage in RDBMS
 */
public class KudosStorage {
  private static final Log LOG = ExoLogger.getLogger(KudosStorage.class);

  private KudosDAO         kudosDAO;

  private IdentityManager  identityManager;

  public KudosStorage(KudosDAO kudosDAO, IdentityManager identityManager) {
    this.kudosDAO = kudosDAO;
    this.identityManager = identityManager;
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
    Identity identity = identityManager.getOrCreateIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                           : SpaceIdentityProvider.NAME,
                                                            receiverId,
                                                            true);
    return kudosDAO.countKudosByPeriodAndReceiver(kudosPeriod,
                                                  Long.parseLong(identity.getId()),
                                                  isReceiverUser);
  }

  public List<Kudos> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, String receiverType, String receiverId) {
    List<Kudos> kudosList = new ArrayList<>();
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    Identity identity = identityManager.getOrCreateIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                           : SpaceIdentityProvider.NAME,
                                                            receiverId,
                                                            true);
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByPeriodAndReceiver(kudosPeriod,
                                                                           Long.parseLong(identity.getId()),
                                                                           isReceiverUser);
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public List<Kudos> getKudosByPeriodAndSender(KudosPeriod kudosPeriod, String senderId) {
    List<Kudos> kudosList = new ArrayList<>();
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
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
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    return kudosDAO.countKudosByPeriodAndSender(kudosPeriod, Long.parseLong(identity.getId()));
  }

}
