/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class KudosPeriodTypeTest {

  @Test
  public void testGetWeekPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.WEEK.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 8).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 7, 15).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetMonthPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.MONTH.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 8, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetQuarterPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.QUARTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2019, 10, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetSemesterPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.SEMESTER.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testGetYearPeriod() {
    KudosPeriod periodOfTime = KudosPeriodType.YEAR.getPeriodOfTime(LocalDate.of(2019, 7, 12).atStartOfDay());
    assertNotNull(periodOfTime);

    long startTimeInSeconds = LocalDate.of(2019, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod expectedPeriod = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertEquals(expectedPeriod, periodOfTime);
    assertEquals(expectedPeriod.hashCode(), periodOfTime.hashCode());
    assertEquals(startTimeInSeconds, periodOfTime.getStartDateInSeconds());
    assertEquals(endTimeInSeconds, periodOfTime.getEndDateInSeconds());
  }

  @Test
  public void testKudosPeriod() throws JSONException {
    long startTimeInSeconds = LocalDate.of(2019, 7, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();
    long endTimeInSeconds = LocalDate.of(2020, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

    KudosPeriod periodOfTime = new KudosPeriod(startTimeInSeconds, endTimeInSeconds);
    assertTrue(periodOfTime.toString().contains(String.valueOf(startTimeInSeconds)));
    assertTrue(periodOfTime.toString().contains(String.valueOf(endTimeInSeconds)));
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("startDateInSeconds", startTimeInSeconds);
    jsonObject.put("endDateInSeconds", endTimeInSeconds);
    assertEquals(jsonObject.toString(), periodOfTime.toJSONObject().toString());
  }
}
