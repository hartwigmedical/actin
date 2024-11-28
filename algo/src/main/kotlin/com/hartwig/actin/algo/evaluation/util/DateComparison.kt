package com.hartwig.actin.algo.evaluation.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object DateComparison {

    fun isAfterDate(minDate: LocalDate, year: Int?, month: Int?): Boolean? {
        return when {
            year == null -> null
            year > minDate.year -> true
            year == minDate.year ->
                if (month == null || month == minDate.monthValue) null else month > minDate.monthValue

            else -> false
        }
    }

    fun isBeforeDate(maxDate: LocalDate, year: Int?, month: Int?): Boolean? {
        return isAfterDate(maxDate, year, month)?.not()
    }

    fun minWeeksBetweenDates(startYear: Int?, startMonth: Int?, stopYear: Int?, stopMonth: Int?): Long? {
        return if (startYear != null && stopYear != null) {
            ChronoUnit.WEEKS.between(
                YearMonth.of(startYear, startMonth ?: 12).atEndOfMonth(),
                YearMonth.of(stopYear, stopMonth ?: 1).atDay(1)
            ).coerceAtLeast(0)
        } else {
            null
        }
    }
}