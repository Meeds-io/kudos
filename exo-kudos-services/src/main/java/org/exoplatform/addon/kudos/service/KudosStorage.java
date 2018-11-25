package org.exoplatform.addon.kudos.service;

import static org.exoplatform.addon.kudos.service.utils.Utils.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.addon.kudos.dao.KudosDAO;
import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
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

  public List<Kudos> getAllKudosByMonth(YearMonth yearMonth) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByMonth(yearMonth);
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public List<Kudos> getAllKudosByMonthAndEntityType(YearMonth yearMonth, String entityType) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByMonthAndEntityType(yearMonth,
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

  public List<Kudos> getKudosByMonthAndReceiver(YearMonth yearMonth, String receiverType, String receiverId) {
    List<Kudos> kudosList = new ArrayList<>();
    boolean isReceiverUser = USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
    Identity identity = identityManager.getOrCreateIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                           : SpaceIdentityProvider.NAME,
                                                            receiverId,
                                                            true);
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByMonthAndReceiver(yearMonth,
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

  public List<Kudos> getKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    List<Kudos> kudosList = new ArrayList<>();
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByMonthAndSender(yearMonth, Long.parseLong(identity.getId()));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  public long countKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    return kudosDAO.countKudosByMonthAndSender(yearMonth, Long.parseLong(identity.getId()));
  }

}
