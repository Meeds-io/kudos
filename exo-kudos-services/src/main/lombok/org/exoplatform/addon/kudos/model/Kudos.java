package org.exoplatform.addon.kudos.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Kudos {
  public Long          technicalId;

  public String        entityId;

  public String        entityType;

  public String        senderId;

  public String        receiverId;

  public String        receiverType;

  public short         num;

  public LocalDateTime time;
}
