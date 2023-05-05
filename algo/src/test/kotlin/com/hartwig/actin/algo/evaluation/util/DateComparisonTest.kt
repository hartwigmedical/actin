package com.hartwig.actin.algo.evaluation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateComparisonTest {
    @Test
    fun canDetermineIfYearMonthAreAfterMinDate() {
        val minDate: LocalDate = LocalDate.of(2020, 6, 20)
        assertNull(DateComparison.isAfterDate(minDate, null, null))
        assertNull(DateComparison.isAfterDate(minDate, 2020, null))
        assertNull(DateComparison.isAfterDate(minDate, 2020, 6))
        assertEquals(false, DateComparison.isAfterDate(minDate, 2019, null))
        assertEquals(false, DateComparison.isAfterDate(minDate, 2020, 4))
        assertEquals(true, DateComparison.isAfterDate(minDate, 2021, null))
        assertEquals(true, DateComparison.isAfterDate(minDate, 2021, 8))
    }

    @Test
    fun canDetermineIfYearMonthAreBeforeMaxDate() {
        val maxDate: LocalDate = LocalDate.of(2020, 6, 20)
        assertNull(DateComparison.isBeforeDate(maxDate, null, null))
        assertNull(DateComparison.isBeforeDate(maxDate, 2020, null))
        assertNull(DateComparison.isBeforeDate(maxDate, 2020, 6))
        assertEquals(true, DateComparison.isBeforeDate(maxDate, 2019, null))
        assertEquals(true, DateComparison.isBeforeDate(maxDate, 2020, 4))
        assertEquals(false, DateComparison.isBeforeDate(maxDate, 2021, null))
        assertEquals(false, DateComparison.isBeforeDate(maxDate, 2021, 8))
    }

    @Test
    fun minWeeksBetweenDatesShouldReturnEmptyForNullStartOrStopYear() {
        assertNull(DateComparison.minWeeksBetweenDates(null, null, null, null))
        assertNull(DateComparison.minWeeksBetweenDates(1900, null, null, null))
        assertNull(DateComparison.minWeeksBetweenDates(null, 2000, null, null))
    }

    @Test
    fun minWeeksBetweenDatesShouldCalculateNumberOfWeeksUsingYearsAndMonths() {
        assertEquals(4L, DateComparison.minWeeksBetweenDates(2023, 1, 2023, 3))
    }

    @Test
    fun minWeeksBetweenDatesShouldAssumeMinimumDurationWhenMonthsNotProvided() {
        assertEquals(52L, DateComparison.minWeeksBetweenDates(2021, null, 2023, null))
    }

    @Test
    fun minWeeksBetweenDatesShouldReturnZeroWhenStopDateIsBeforeStartDate() {
        assertEquals(0L, DateComparison.minWeeksBetweenDates(2023, 3, 2023, 1))
    }
}