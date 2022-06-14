package org.exoplatform.kudos.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSettings {

  private boolean disabled;

  private long    remainingKudos;

}
