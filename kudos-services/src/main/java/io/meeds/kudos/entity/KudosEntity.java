/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.entity;

import java.io.Serializable;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

@Entity(name = "Kudos")
@DynamicUpdate
@Table(name = "ADDONS_KUDOS")
public class KudosEntity implements Serializable {

  private static final long serialVersionUID = -8272292325540761902L;

  @Id
  @SequenceGenerator(name = "SEQ_ADDONS_KUDOS_ID", sequenceName = "SEQ_ADDONS_KUDOS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ADDONS_KUDOS_ID")
  @Column(name = "KUDOS_ID")
  private Long              id;

  @Column(name = "SENDER_ID", nullable = false)
  public long               senderId;

  @Column(name = "RECEIVER_ID", nullable = false)
  public long               receiverId;

  @Column(name = "IS_RECEIVER_USER", nullable = false)
  public boolean            isReceiverUser;

  @Column(name = "PARENT_ENTITY_ID", nullable = true)
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
