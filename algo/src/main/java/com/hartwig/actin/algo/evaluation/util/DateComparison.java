package com.hartwig.actin.algo.evaluation.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DateComparison {

    //    private static final double WEEKS_PER_MONTH = 52 / 12D;

    private DateComparison() {
    }

    @Nullable
    public static Boolean isAfterDate(@NotNull LocalDate minDate, @Nullable Integer year, @Nullable Integer month) {
        if (year == null) {
            return null;
        } else if (year > minDate.getYear()) {
            return true;
        } else if (year < minDate.getYear()) {
            return false;
        }

        // Year is equal, check month
        if (month == null || month == minDate.getMonthValue()) {
            return null;
        } else {
            return month > minDate.getMonthValue();
        }
    }

    @Nullable
    public static Boolean isBeforeDate(@NotNull LocalDate maxDate, @Nullable Integer year, @Nullable Integer month) {
        Boolean isAfterDate = isAfterDate(maxDate, year, month);
        return isAfterDate != null ? !isAfterDate : null;
    }

    public static Optional<Long> minWeeksBetweenDates(@Nullable Integer startYear, @Nullable Integer startMonth, @Nullable Integer stopYear,
            @Nullable Integer stopMonth) {
        if (startYear != null && stopYear != null) {
            return Optional.of(Math.max(0,
                    ChronoUnit.WEEKS.between(YearMonth.of(startYear, Optional.ofNullable(startMonth).orElse(12)).atEndOfMonth(),
                            YearMonth.of(stopYear, Optional.ofNullable(stopMonth).orElse(1)).atDay(1))));
        } else {
            return Optional.empty();
        }
    }
}
