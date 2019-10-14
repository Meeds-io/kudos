package org.exoplatform.kudos.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "Kudos")
@ExoEntity
@DynamicUpdate
@Table(name = "ADDONS_KUDOS")
@NamedQueries({
    @NamedQuery(name = "Kudos.getAllKudosByPeriod", query = "select k from Kudos k" + " where k.createdDate > :startDate"
        + " AND k.createdDate < :endDate"),
    @NamedQuery(name = "Kudos.getAllKudosByPeriodAndEntityType", query = "select k from Kudos k"
        + " where k.entityType = :entityType" + " AND k.createdDate > :startDate" + " AND k.createdDate < :endDate"),
    @NamedQuery(name = "Kudos.getAllKudosByEntity", query = "select k from Kudos k" + " where k.entityType = :entityType"
        + " AND k.entityId = :entityId"),
    @NamedQuery(name = "Kudos.getAllKudosByParentEntity", query = "select k from Kudos k" + " where k.entityType = :entityType"
        + " AND (k.parentEntityId = :parentEntityId "
        + " OR k.entityId = :parentEntityId)"),
    @NamedQuery(name = "Kudos.getKudosByPeriodAndSender", query = "select k from Kudos k" + " where k.senderId = :senderId"
        + " AND k.createdDate > :startDate" + " AND k.createdDate < :endDate"),
    @NamedQuery(name = "Kudos.countKudosByPeriodAndSender", query = "select count(k) from Kudos k"
        + " where k.senderId = :senderId" + " AND k.createdDate > :startDate" + " AND k.createdDate < :endDate"),
    @NamedQuery(name = "Kudos.getKudosByPeriodAndReceiver", query = "select k from Kudos k" + " where k.receiverId = :receiverId"
        + " AND k.isReceiverUser = :isReceiverUser" + " AND k.createdDate > :startDate" + " AND k.createdDate < :endDate"),
    @NamedQuery(name = "Kudos.countKudosByPeriodAndReceiver", query = "select count(k) from Kudos k"
        + " where k.receiverId = :receiverId"
        + " AND k.isReceiverUser = :isReceiverUser" + " AND k.createdDate > :startDate" + " AND k.createdDate < :endDate") })
public class KudosEntity implements Serializable {

  private static final long serialVersionUID = -8272292325540761902L;

  @Id
  @SequenceGenerator(name = "SEQ_ADDONS_KUDOS_ID", sequenceName = "SEQ_ADDONS_KUDOS_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ADDONS_KUDOS_ID")
  @Column(name = "KUDOS_ID")
  private Long              id;

  @Column(name = "SENDER_ID", nullable = false)
  public long               senderId;

  @Column(name = "RECEIVER_ID", nullable = false)
  public long               receiverId;

  @Column(name = "IS_RECEIVER_USER", nullable = false)
  public boolean            isReceiverUser;

  @Column(name = "PARENT_ENTITY_ID", nullable = false)
  public Long               parentEntityId;

  @Column(name = "ENTITY_ID", nullable = false)
  public long               entityId;

  @Column(name = "ENTITY_TYPE", nullable = false)
  public int                entityType;

  @Column(name = "ACTIVITY_ID")
  public Long               activityId;

  @Column(name = "MESSAGE", nullable = true)
  public String             message;

  @Column(name = "CREATED_DATE", nullable = false)
  public long               createdDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getSenderId() {
    return senderId;
  }

  public void setSenderId(long senderId) {
    this.senderId = senderId;
  }

  public long getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(long receiverId) {
    this.receiverId = receiverId;
  }

  public boolean isReceiverUser() {
    return isReceiverUser;
  }

  public void setReceiverUser(boolean isReceiverUser) {
    this.isReceiverUser = isReceiverUser;
  }

  public long getEntityId() {
    return entityId;
  }

  public void setEntityId(long entityId) {
    this.entityId = entityId;
  }

  public int getEntityType() {
    return entityType;
  }

  public void setEntityType(int entityType) {
    this.entityType = entityType;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public Long getParentEntityId() {
    return parentEntityId;
  }

  public void setParentEntityId(Long parentEntityId) {
    this.parentEntityId = parentEntityId;
  }

  public long getActivityId() {
    return activityId == null ? 0 : activityId;
  }

  public void setActivityId(long activityId) {
    this.activityId = activityId;
  }

}
