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
package io.meeds.kudos.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
public class Kudos implements Serializable {
  private static final long serialVersionUID = -3058584349224472943L;

  private Long              technicalId;

  private String            parentEntityId;

  private String            entityId;

  private String            entityType;

  private String            senderId;

  private String            senderIdentityId;

  private String            senderURL;

  private String            senderAvatar;

  private String            senderFullName;

  private String            receiverId;

  private String            receiverIdentityId;

  private String            receiverType;

  private String            receiverFullName;

  private String            receiverURL;

  private String            receiverAvatar;

  private String            receiverPosition;

  private boolean           externalReceiver;

  private boolean           enabledReceiver;

  private String            message;

  private long              timeInSeconds;

  @Exclude
  private String            spacePrettyName;

  @Exclude
  private long              activityId;

}
