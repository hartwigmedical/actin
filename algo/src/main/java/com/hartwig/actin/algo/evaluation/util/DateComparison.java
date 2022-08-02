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
}
