package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.util.ApplicationConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Format {
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val PERCENTAGE_FORMAT: DecimalFormat = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(ApplicationConfig.LOCALE))
    fun concat(strings: Iterable<String>): String {
        return strings.distinct().sorted().joinToString("; ")
    }

    fun concatLowercaseWithAnd(strings: Iterable<String>): String {
        return strings.map { it.lowercase() }.distinct().sorted().joinToString(" and ")
    }

    fun date(date: LocalDate): String {
        return DATE_FORMAT.format(date)
    }

    fun percentage(fraction: Double): String {
        require(!(fraction < 0 || fraction > 1)) { "Fraction provided that is not within 0 and 1: $fraction" }
        return PERCENTAGE_FORMAT.format(fraction * 100)
    }
}