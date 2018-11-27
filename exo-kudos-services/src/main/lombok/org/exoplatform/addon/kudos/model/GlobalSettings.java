package org.exoplatform.addon.kudos.model;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.Data;

@Data
public class GlobalSettings {
  String          accessPermission;

  long            kudosPerPeriod;

  KudosPeriodType kudosPeriodType = KudosPeriodType.DEFAULT;

  public JSONObject toJSONObject(boolean includeTransient) {
    JSONObject jsonObject = new JSONObject();
    try {
      if (accessPermission != null) {
        jsonObject.put("accessPermission", accessPermission);
      }
      jsonObject.put("kudosPerPeriod", kudosPerPeriod);
      jsonObject.put("kudosPeriodType", kudosPeriodType.name());
      if (includeTransient) {
        jsonObject.put("startPeriodDateInSeconds", kudosPeriodType.getPeriodOfTime(LocalDateTime.now()).getStartDateInSeconds());
        jsonObject.put("endPeriodDateInSeconds", kudosPeriodType.getPeriodOfTime(LocalDateTime.now()).getEndDateInSeconds());
      }
    } catch (JSONException e) {
      throw new RuntimeException("Error while converting Object to JSON", e);
    }
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJSONObject(true).toString();
  }

  public String toStringToPersist() {
    return toJSONObject(false).toString();
  }

  public static final GlobalSettings parseStringToObject(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return null;
    }

    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      GlobalSettings globalSettings = new GlobalSettings();
      globalSettings.setAccessPermission(jsonObject.has("accessPermission") ? jsonObject.getString("accessPermission") : null);
      globalSettings.setKudosPerPeriod(jsonObject.has("kudosPerPeriod") ? jsonObject.getLong("kudosPerPeriod") : 0);
      globalSettings.setKudosPeriodType(jsonObject.has("kudosPeriodType") ? KudosPeriodType.valueOf(jsonObject.getString("kudosPeriodType")
                                                                                                              .toUpperCase())
                                                                          : KudosPeriodType.DEFAULT);
      return globalSettings;
    } catch (JSONException e) {
      throw new RuntimeException("Error while converting JSON String to Object", e);
    }
  }

}
