package org.exoplatform.addon.kudos.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Kudos implements Comparable<Kudos> {
  public Long          technicalId;

  public String        entityId;

  public String        entityType;

  public String        senderId;

  public String        receiverId;

  public String        receiverType;

  public String        receiverFullName;

  public String        receiverURL;

  public String        message;

  public LocalDateTime time;

  @Override
  public int compareTo(Kudos o) {
    if (this.getTime() == null) {
      return -1;
    }
    if (o.getTime() == null) {
      return 1;
    }
    return this.getTime().compareTo(o.getTime());
  }
}
