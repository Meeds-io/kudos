package org.exoplatform.addon.kudos.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Kudos implements Comparable<Kudos> {
  private Long          technicalId;

  private String        entityId;

  private String        entityType;

  private String        senderId;

  private String        receiverId;

  private String        receiverType;

  private String        receiverFullName;

  private String        receiverURL;

  private String        message;

  private LocalDateTime time;

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
