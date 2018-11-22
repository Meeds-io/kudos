package org.exoplatform.addon.kudos.model;

import lombok.Data;

@Data
public class GlobalSettings {
  String accessPermission;

  long   kudosPerMonth;
}
