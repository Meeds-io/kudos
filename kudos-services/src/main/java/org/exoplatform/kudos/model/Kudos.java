package org.exoplatform.kudos.model;

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
