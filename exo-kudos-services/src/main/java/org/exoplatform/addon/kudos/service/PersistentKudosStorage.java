package org.exoplatform.addon.kudos.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.addon.kudos.dao.KudosDAO;
import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
import org.exoplatform.addon.kudos.service.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * This is a placeholder class to manage Kudos storage in a transient way
 */
public class PersistentKudosStorage implements KudosStorage {
  private static final Log LOG = ExoLogger.getLogger(PersistentKudosStorage.class);

  private KudosDAO         kudosDAO;

  private IdentityManager  identityManager;

  public PersistentKudosStorage(KudosDAO kudosDAO, IdentityManager identityManager) {
    this.kudosDAO = kudosDAO;
    this.identityManager = identityManager;
  }

  @Override
  public Kudos getKudoById(long id) {
    KudosEntity kudosEntity = kudosDAO.find(id);
    if (kudosEntity == null) {
      LOG.warn("Can't find Kudos with id {}", id);
      return null;
    } else {
      return Utils.fromEntity(kudosEntity);
    }
  }

  @Override
  public void createKudos(Kudos kudos) {
    KudosEntity kudosEntity = Utils.toNewEntity(kudos);
    kudosDAO.create(kudosEntity);
  }

  @Override
  public List<Kudos> getAllKudosByMonth(YearMonth yearMonth) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByMonth(yearMonth);
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(Utils.fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  @Override
  public List<Kudos> getAllKudosByMonthAndEntityType(YearMonth yearMonth, String entityType) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByMonthAndEntityType(yearMonth,
                                                                               KudosEntityType.valueOf(entityType).ordinal());
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(Utils.fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  @Override
  public List<Kudos> getAllKudosByEntity(String entityType, String entityId) {
    List<Kudos> kudosList = new ArrayList<>();
    List<KudosEntity> kudosEntities = kudosDAO.getAllKudosByEntity(KudosEntityType.valueOf(entityType).ordinal(),
                                                                   Long.parseLong(entityId));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(Utils.fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  @Override
  public List<Kudos> getKudosByMonthAndReceiver(YearMonth yearMonth, String receiverType, String receiverId) {
    List<Kudos> kudosList = new ArrayList<>();
    boolean isReceiverUser =
                           Utils.USER_ACCOUNT_TYPE.equals(receiverType) || OrganizationIdentityProvider.NAME.equals(receiverType);
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
          kudosList.add(Utils.fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  @Override
  public List<Kudos> getKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    List<Kudos> kudosList = new ArrayList<>();
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    List<KudosEntity> kudosEntities = kudosDAO.getKudosByMonthAndSender(yearMonth, Long.parseLong(identity.getId()));
    if (kudosEntities != null) {
      for (KudosEntity kudosEntity : kudosEntities) {
        if (kudosEntity != null) {
          kudosList.add(Utils.fromEntity(kudosEntity));
        }
      }
    }
    return kudosList;
  }

  @Override
  public long countKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    return kudosDAO.countKudosByMonthAndSender(yearMonth, Long.parseLong(identity.getId()));
  }

}
