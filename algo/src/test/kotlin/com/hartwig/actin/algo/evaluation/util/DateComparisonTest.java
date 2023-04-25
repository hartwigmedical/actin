package com.hartwig.actin.algo.evaluation.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class DateComparisonTest {

    @Test
    public void canDetermineIfYearMonthAreAfterMinDate() {
        LocalDate minDate = LocalDate.of(2020, 6, 20);

        assertNull(DateComparison.isAfterDate(minDate, null, null));
        assertNull(DateComparison.isAfterDate(minDate, 2020, null));
        assertNull(DateComparison.isAfterDate(minDate, 2020, 6));

        assertFalse(DateComparison.isAfterDate(minDate, 2019, null));
        assertFalse(DateComparison.isAfterDate(minDate, 2020, 4));

        assertTrue(DateComparison.isAfterDate(minDate, 2021, null));
        assertTrue(DateComparison.isAfterDate(minDate, 2021, 8));
    }

    @Test
    public void canDetermineIfYearMonthAreBeforeMaxDate() {
        LocalDate maxDate = LocalDate.of(2020, 6, 20);

        assertNull(DateComparison.isBeforeDate(maxDate, null, null));
        assertNull(DateComparison.isBeforeDate(maxDate, 2020, null));
        assertNull(DateComparison.isBeforeDate(maxDate, 2020, 6));

        assertTrue(DateComparison.isBeforeDate(maxDate, 2019, null));
        assertTrue(DateComparison.isBeforeDate(maxDate, 2020, 4));

        assertFalse(DateComparison.isBeforeDate(maxDate, 2021, null));
        assertFalse(DateComparison.isBeforeDate(maxDate, 2021, 8));
    }

    @Test
    public void minWeeksBetweenDatesShouldReturnEmptyForNullStartOrStopYear() {
        assertThat(DateComparison.minWeeksBetweenDates(null, null, null, null)).isEmpty();
        assertThat(DateComparison.minWeeksBetweenDates(1900, null, null, null)).isEmpty();
        assertThat(DateComparison.minWeeksBetweenDates(null, 2000, null, null)).isEmpty();
    }

    @Test
    public void minWeeksBetweenDatesShouldCalculateNumberOfWeeksUsingYearsAndMonths() {
        assertThat(DateComparison.minWeeksBetweenDates(2023, 1, 2023, 3)).contains(4L);
    }

    @Test
    public void minWeeksBetweenDatesShouldAssumeMinimumDurationWhenMonthsNotProvided() {
        assertThat(DateComparison.minWeeksBetweenDates(2021, null, 2023, null)).contains(52L);
    }

    @Test
    public void minWeeksBetweenDatesShouldReturnZeroWhenStopDateIsBeforeStartDate() {
        assertThat(DateComparison.minWeeksBetweenDates(2023, 3, 2023, 1)).contains(0L);
    }
}