package org.exoplatform.kudos.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.KudosPeriod;

public class KudosDAO extends GenericDAOJPAImpl<KudosEntity, Long> {

  public List<KudosEntity> getKudosByPeriod(KudosPeriod kudosPeriod, int limit) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriod", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByPeriodAndEntityType(KudosPeriod kudosPeriod, int entityType, int limit) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriodAndEntityType",
                                                                        KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("entityType", entityType);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByEntity(int entityType, long entityId, int limit) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByEntity", KudosEntity.class);
    query.setParameter("entityId", entityId);
    query.setParameter("entityType", entityType);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod,
                                                       long receiverId,
                                                       boolean isReceiverUser,
                                                       int limit) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriodAndReceiver", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("receiverId", receiverId);
    query.setParameter("isReceiverUser", isReceiverUser);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  public long countKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, long receiverId, boolean isReceiverUser) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Kudos.countKudosByPeriodAndReceiver", Long.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("receiverId", receiverId);
    query.setParameter("isReceiverUser", isReceiverUser);
    Long count = query.getSingleResult();
    return count == null ? 0 : count;
  }

  public List<KudosEntity> getKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderId, int limit) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriodAndSender", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("senderId", senderId);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  public long countKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Kudos.countKudosByPeriodAndSender", Long.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("senderId", senderId);
    return query.getSingleResult();
  }

  private void setPeriodParameters(TypedQuery<?> query, KudosPeriod kudosPeriod) {
    query.setParameter("startDate", kudosPeriod.getStartDateInSeconds());
    query.setParameter("endDate", kudosPeriod.getEndDateInSeconds());
  }

}
