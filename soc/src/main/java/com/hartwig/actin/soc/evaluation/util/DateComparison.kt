package com.hartwig.actin.soc.evaluation.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object DateComparison {
    fun isAfterDate(minDate: LocalDate, year: Int?, month: Int?): Boolean? {
        if (year == null) {
            return null
        } else if (year > minDate.year) {
            return true
        } else if (year < minDate.year) {
            return false
        }

        // Year is equal, check month
        return if (month == null || month == minDate.monthValue) {
            null
        } else {
            month > minDate.monthValue
        }
    }

    fun isBeforeDate(maxDate: LocalDate, year: Int?, month: Int?): Boolean? {
        val isAfterDate = isAfterDate(maxDate, year, month)
        return if (isAfterDate != null) !isAfterDate else null
    }

    fun minWeeksBetweenDates(startYear: Int?, startMonth: Int?, stopYear: Int?, stopMonth: Int?): Long? {
        return if (startYear != null && stopYear != null) {
            ChronoUnit.WEEKS.between(YearMonth.of(startYear, startMonth ?: 12).atEndOfMonth(),
                    YearMonth.of(stopYear, stopMonth ?: 1).atDay(1)).coerceAtLeast(0)
        } else {
            null
        }
    }
}