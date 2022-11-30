package com.hartwig.actin.algo.evaluation.util;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DateComparison {

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

    @Nullable
    public static Integer minMonthsBetweenDates(@Nullable Integer startYear, @Nullable Integer startMonth, @Nullable Integer stopYear,
            @Nullable Integer stopMonth) {
        //TODO: Implement such that it calculates the minimal nr of weeks that passed by based on start/stop year+month.  In case min is negative, resolve to 0. In case of missing all stop or start, resolve to null
        return null;
    }
}
