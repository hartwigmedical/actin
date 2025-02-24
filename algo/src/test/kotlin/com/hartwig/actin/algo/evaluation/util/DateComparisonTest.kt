package com.hartwig.actin.algo.evaluation.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class DateComparisonTest {

    @Test
    fun `Should determine if year and month are after min date`() {
        val minDate = LocalDate.of(2020, 6, 20)
        assertThat(DateComparison.isAfterDate(minDate, null, null)).isNull()
        assertThat(DateComparison.isAfterDate(minDate, 2020, null)).isNull()
        assertThat(DateComparison.isAfterDate(minDate, 2020, 6)).isNull()
        assertThat(DateComparison.isAfterDate(minDate, 2019, null)).isFalse()
        assertThat(DateComparison.isAfterDate(minDate, 2020, 4)).isFalse()
        assertThat(DateComparison.isAfterDate(minDate, 2021, null)).isTrue()
        assertThat(DateComparison.isAfterDate(minDate, 2021, 8)).isTrue()
    }

    @Test
    fun `Should determine if year and month are before max date`() {
        val maxDate = LocalDate.of(2020, 6, 20)
        assertThat(DateComparison.isBeforeDate(maxDate, null, null)).isNull()
        assertThat(DateComparison.isBeforeDate(maxDate, 2020, null)).isNull()
        assertThat(DateComparison.isBeforeDate(maxDate, 2020, 6)).isNull()
        assertThat(DateComparison.isBeforeDate(maxDate, 2019, null)).isTrue()
        assertThat(DateComparison.isBeforeDate(maxDate, 2020, 4)).isTrue()
        assertThat(DateComparison.isBeforeDate(maxDate, 2021, null)).isFalse()
        assertThat(DateComparison.isBeforeDate(maxDate, 2021, 8)).isFalse()
    }

    @Test
    fun `Should determine if year and month are exactly the same as reference date`() {
        val refMonth = 6
        val refYear = 2020
        val refDate = LocalDate.of(refYear, refMonth, 20)
        assertThat(DateComparison.isExactYearAndMonth(refDate, null, null)).isFalse()
        assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, null)).isFalse()
        assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, refMonth)).isTrue()
        assertThat(DateComparison.isExactYearAndMonth(refDate, null, refMonth)).isFalse()
        assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, refMonth.minus(1))).isFalse()
        assertThat(DateComparison.isExactYearAndMonth(refDate, refYear.minus(1), refMonth)).isFalse()
    }

    @Test
    fun `Should return null when start or stop year is null`() {
        assertThat(DateComparison.minWeeksBetweenDates(null, null, null, null)).isNull()
        assertThat(DateComparison.minWeeksBetweenDates(1900, null, null, null)).isNull()
        assertThat(DateComparison.minWeeksBetweenDates(null, 2000, null, null)).isNull()
    }

    @Test
    fun `Should calculate min weeks using years and months`() {
        assertThat(DateComparison.minWeeksBetweenDates(2023, 1, 2023, 3)).isEqualTo(4)
    }

    @Test
    fun `Should assume minimum duration when months are not provided`() {
        assertThat(DateComparison.minWeeksBetweenDates(2021, null, 2023, null)).isEqualTo(52)
    }

    @Test
    fun `Should return zero when stop date is before start date`() {
        assertThat(DateComparison.minWeeksBetweenDates(2023, 3, 2023, 1)).isEqualTo(0)
    }
}