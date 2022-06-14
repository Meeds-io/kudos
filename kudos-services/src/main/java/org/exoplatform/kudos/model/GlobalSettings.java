package org.exoplatform.kudos.model;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSettings implements Cloneable {
  private static final String END_PERIOD_DATE_IN_SECONDS_PARAM   = "endPeriodDateInSeconds";

  private static final String START_PERIOD_DATE_IN_SECONDS_PARAM = "startPeriodDateInSeconds";

  private static final String KUDOS_PERIOD_TYPE_PARAM            = "kudosPeriodType";

  private static final String KUDOS_PER_PERIOD_PARAM             = "kudosPerPeriod";

  private static final String ACCESS_PERMISSION_PARAM            = "accessPermission";

  String                      accessPermission;

  long                        kudosPerPeriod;

  KudosPeriodType             kudosPeriodType                    = KudosPeriodType.DEFAULT;

  public JSONObject toJSONObject(boolean includeTransient) {
    JSONObject jsonObject = new JSONObject();
    try {
      if (accessPermission != null) {
        jsonObject.put(ACCESS_PERMISSION_PARAM, accessPermission);
      }
      jsonObject.put(KUDOS_PER_PERIOD_PARAM, kudosPerPeriod);
      jsonObject.put(KUDOS_PERIOD_TYPE_PARAM, kudosPeriodType.name());
      if (includeTransient) {
        jsonObject.put(START_PERIOD_DATE_IN_SECONDS_PARAM,
                       kudosPeriodType.getPeriodOfTime(LocalDateTime.now()).getStartDateInSeconds());
        jsonObject.put(END_PERIOD_DATE_IN_SECONDS_PARAM,
                       kudosPeriodType.getPeriodOfTime(LocalDateTime.now()).getEndDateInSeconds());
      }
    } catch (JSONException e) {
      throw new IllegalStateException("Error while converting Object to JSON", e);
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
      globalSettings.setAccessPermission(jsonObject.has(ACCESS_PERMISSION_PARAM) ? jsonObject.getString(ACCESS_PERMISSION_PARAM)
                                                                                 : null);
      globalSettings.setKudosPerPeriod(jsonObject.has(KUDOS_PER_PERIOD_PARAM) ? jsonObject.getLong(KUDOS_PER_PERIOD_PARAM) : 0);
      globalSettings.setKudosPeriodType(jsonObject.has(KUDOS_PERIOD_TYPE_PARAM) ? KudosPeriodType.valueOf(jsonObject.getString(KUDOS_PERIOD_TYPE_PARAM)
                                                                                                                    .toUpperCase())
                                                                                : KudosPeriodType.DEFAULT);
      return globalSettings;
    } catch (JSONException e) {
      throw new IllegalStateException("Error while converting JSON String to Object", e);
    }
  }

  @Override
  public GlobalSettings clone() { // NOSONAR
    try {
      return (GlobalSettings) super.clone();
    } catch (CloneNotSupportedException e) {
      return new GlobalSettings(accessPermission, kudosPerPeriod, kudosPeriodType);
    }
  }
}
