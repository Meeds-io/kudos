package org.exoplatform.kudos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
public class Kudos implements Comparable<Kudos>, Serializable {
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

  private String            message;

  private LocalDateTime     time;

  @Exclude
  private long              activityId;

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
