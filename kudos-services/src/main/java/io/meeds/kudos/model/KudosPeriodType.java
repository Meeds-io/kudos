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

import static io.meeds.kudos.service.utils.Utils.timeToSeconds;

import java.time.*;

public enum KudosPeriodType {
  WEEK, MONTH, QUARTER, SEMESTER, YEAR;

  public static final KudosPeriodType DEFAULT = WEEK;

  public KudosPeriod getPeriodOfTime(LocalDateTime localDateTime) {
    KudosPeriod kudosPeriod = new KudosPeriod();
    YearMonth yearMonth = YearMonth.from(localDateTime);
    switch (this) {
    case WEEK:
      LocalDateTime firstDayOfThisWeek = localDateTime.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
      LocalDateTime firstDayOfNextWeek = firstDayOfThisWeek.plusWeeks(1);
      kudosPeriod.setStartDateInSeconds(timeToSeconds(firstDayOfThisWeek));
      kudosPeriod.setEndDateInSeconds(timeToSeconds(firstDayOfNextWeek));
      break;
    case MONTH:
      YearMonth currentMonth = yearMonth;
      YearMonth nextMonth = currentMonth.plusMonths(1);
      kudosPeriod.setStartDateInSeconds(timeToSeconds(currentMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDateInSeconds(timeToSeconds(nextMonth.atDay(1).atStartOfDay()));
      break;
    case QUARTER:
      int monthQuarterIndex = ((yearMonth.getMonthValue() - 1) / 3) * 3 + 1;

      YearMonth startQuarterMonth = YearMonth.of(yearMonth.getYear(), monthQuarterIndex);
      YearMonth endQuarterMonth = startQuarterMonth.plusMonths(3);
      kudosPeriod.setStartDateInSeconds(timeToSeconds(startQuarterMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDateInSeconds(timeToSeconds(endQuarterMonth.atDay(1).atStartOfDay()));
      break;
    case SEMESTER:
      int monthSemesterIndex = ((yearMonth.getMonthValue() - 1) / 6) * 6 + 1;

      YearMonth startSemesterMonth = YearMonth.of(yearMonth.getYear(), monthSemesterIndex);
      YearMonth endSemesterMonth = startSemesterMonth.plusMonths(6);
      kudosPeriod.setStartDateInSeconds(timeToSeconds(startSemesterMonth.atDay(1).atStartOfDay()));
      kudosPeriod.setEndDateInSeconds(timeToSeconds(endSemesterMonth.atDay(1).atStartOfDay()));
      break;
    case YEAR:
      kudosPeriod.setStartDateInSeconds(timeToSeconds(Year.from(localDateTime).atDay(1).atStartOfDay()));
      kudosPeriod.setEndDateInSeconds(timeToSeconds(Year.from(localDateTime).plusYears(1).atDay(1).atStartOfDay()));
      break;
    }
    return kudosPeriod;
  }
}
