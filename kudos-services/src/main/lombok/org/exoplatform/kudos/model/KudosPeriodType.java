package org.exoplatform.kudos.model;

import static org.exoplatform.kudos.service.utils.Utils.timeToSeconds;

import java.time.*;

public enum KudosPeriodType {
  WEEK, MONTH, QUARTER, SEMESTER, YEAR;

  public static final KudosPeriodType DEFAULT = MONTH;

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
