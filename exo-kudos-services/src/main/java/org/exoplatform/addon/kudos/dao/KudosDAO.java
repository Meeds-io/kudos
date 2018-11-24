package org.exoplatform.addon.kudos.dao;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class KudosDAO extends GenericDAOJPAImpl<KudosEntity, Long> {
  @Override
  public void deleteAll() {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  @Override
  public void delete(KudosEntity entity) {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  @Override
  public void deleteAll(List<KudosEntity> entities) {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  public List<KudosEntity> getAllKudosByMonth(YearMonth yearMonth) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByMonth", KudosEntity.class);
    setMonthParameters(query, yearMonth);
    return query.getResultList();
  }

  public List<KudosEntity> getAllKudosByMonthAndEntityType(YearMonth yearMonth, int entityType) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByMonthAndEntityType", KudosEntity.class);
    setMonthParameters(query, yearMonth);
    query.setParameter("entityType", entityType);
    return query.getResultList();
  }

  public List<KudosEntity> getAllKudosByEntity(int entityType, long entityId) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByEntity", KudosEntity.class);
    query.setParameter("entityId", entityId);
    query.setParameter("entityType", entityType);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByMonthAndReceiver(YearMonth yearMonth, long receiverId, boolean isReceiverUser) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByMonthAndReceiver", KudosEntity.class);
    setMonthParameters(query, yearMonth);
    query.setParameter("receiverId", receiverId);
    query.setParameter("isReceiverUser", isReceiverUser);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByMonthAndSender(YearMonth yearMonth, long senderId) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByMonthAndSender", KudosEntity.class);
    setMonthParameters(query, yearMonth);
    query.setParameter("senderId", senderId);
    return query.getResultList();
  }

  public long countKudosByMonthAndSender(YearMonth yearMonth, long senderId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Kudos.countKudosByMonthAndSender", Long.class);
    setMonthParameters(query, yearMonth);
    query.setParameter("senderId", senderId);
    return query.getSingleResult();
  }

  private void setMonthParameters(TypedQuery<?> query, YearMonth yearMonth) {
    long monthStartTimeInSeconds = yearMonth.atDay(1).atStartOfDay(ZoneOffset.systemDefault()).toEpochSecond();
    long monthEndTimeInSeconds = yearMonth.atEndOfMonth().atStartOfDay(ZoneOffset.systemDefault()).toEpochSecond();
    query.setParameter("startDate", monthStartTimeInSeconds);
    query.setParameter("endDate", monthEndTimeInSeconds);
  }

}
