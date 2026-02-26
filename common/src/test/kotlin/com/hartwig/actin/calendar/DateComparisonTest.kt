package com.hartwig.actin.calendar

import java.time.LocalDate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DateComparisonTest {

    @Test
    fun `Should determine if year and month are after min date`() {
        val minDate = LocalDate.of(2020, 6, 20)
        Assertions.assertThat(DateComparison.isAfterDate(minDate, null, null)).isNull()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2020, null)).isNull()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2020, 6)).isNull()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2019, null)).isFalse()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2020, 4)).isFalse()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2021, null)).isTrue()
        Assertions.assertThat(DateComparison.isAfterDate(minDate, 2021, 8)).isTrue()
    }

    @Test
    fun `Should determine if year and month are before max date`() {
        val maxDate = LocalDate.of(2020, 6, 20)
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, null, null)).isNull()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2020, null)).isNull()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2020, 6)).isNull()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2019, null)).isTrue()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2020, 4)).isTrue()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2021, null)).isFalse()
        Assertions.assertThat(DateComparison.isBeforeDate(maxDate, 2021, 8)).isFalse()
    }

    @Test
    fun `Should determine if year and month are exactly the same as reference date`() {
        val refMonth = 6
        val refYear = 2020
        val refDate = LocalDate.of(refYear, refMonth, 20)
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, null, null)).isFalse()
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, null)).isFalse()
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, refMonth)).isTrue()
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, null, refMonth)).isFalse()
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, refYear, refMonth.minus(1))).isFalse()
        Assertions.assertThat(DateComparison.isExactYearAndMonth(refDate, refYear.minus(1), refMonth)).isFalse()
    }

    @Test
    fun `Should return null when start or stop year is null`() {
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(null, null, null, null)).isNull()
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(1900, null, null, null)).isNull()
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(null, 2000, null, null)).isNull()
    }

    @Test
    fun `Should calculate min weeks using years and months`() {
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(2023, 1, 2023, 3)).isEqualTo(4)
    }

    @Test
    fun `Should assume minimum duration when months are not provided`() {
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(2021, null, 2023, null)).isEqualTo(52)
    }

    @Test
    fun `Should return zero when stop date is before start date`() {
        Assertions.assertThat(DateComparison.minWeeksBetweenDates(2023, 3, 2023, 1)).isEqualTo(0)
    }
}