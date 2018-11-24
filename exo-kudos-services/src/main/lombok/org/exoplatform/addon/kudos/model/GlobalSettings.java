package org.exoplatform.addon.kudos.model;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.Data;

@Data
public class GlobalSettings {
  String accessPermission;

  long   kudosPerMonth;

  public JSONObject toJSONObject() {
    JSONObject jsonObject = new JSONObject();
    try {
      if (accessPermission != null) {
        jsonObject.put("accessPermission", accessPermission);
      }
      jsonObject.put("kudosPerMonth", kudosPerMonth);
    } catch (JSONException e) {
      throw new RuntimeException("Error while converting Object to JSON", e);
    }
    return jsonObject;
  }

  @Override
  public String toString() {
    return toJSONObject().toString();
  }

  public static final GlobalSettings parseStringToObject(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return null;
    }

    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      GlobalSettings globalSettings = new GlobalSettings();
      globalSettings.setAccessPermission(jsonObject.has("accessPermission") ? jsonObject.getString("accessPermission") : null);
      globalSettings.setKudosPerMonth(jsonObject.has("kudosPerMonth") ? jsonObject.getLong("kudosPerMonth") : null);
      return globalSettings;
    } catch (JSONException e) {
      throw new RuntimeException("Error while converting JSON String to Object", e);
    }
  }

}
