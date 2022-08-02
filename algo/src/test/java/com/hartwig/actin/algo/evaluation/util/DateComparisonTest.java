package com.hartwig.actin.algo.evaluation.util;

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
}