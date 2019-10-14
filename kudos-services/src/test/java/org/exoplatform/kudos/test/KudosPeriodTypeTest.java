package org.exoplatform.kudos.test;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import org.exoplatform.kudos.model.KudosPeriod;
import org.exoplatform.kudos.model.KudosPeriodType;

public class KudosPeriodTypeTest {

  @Test
  public void testGetWeekPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.WEEK.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 8).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 7, 15).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetMonthPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.MONTH.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 8, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetQuarterPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.QUARTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 10, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetSemesterPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.SEMESTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetYearPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.YEAR.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(expectedPeriod.hashCode(), periodOfTime.hashCode());
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testKudosPeriod() throws JSONException {
    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();

    KudosPeriod periodOfTime = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertTrue(periodOfTime.toString().contains(String.valueOf(startTimeInSeconds)));
    assertTrue(periodOfTime.toString().contains(String.valueOf(endTimeInSeconds)));
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("startDateInSeconds", startTimeInSeconds);
    jsonObject.put("endDateInSeconds", endTimeInSeconds);
    assertEquals(jsonObject.toString(), periodOfTime.toJSONObject().toString());
  }
}
