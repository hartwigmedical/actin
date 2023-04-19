package com.hartwig.actin.soc.evaluation.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object Format {
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val PERCENTAGE_FORMAT: DecimalFormat = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    fun concat(strings: Iterable<String>): String {
        return strings.distinct().joinToString("; ")
    }

    fun date(date: LocalDate): String {
        return DATE_FORMAT.format(date)
    }

    fun percentage(fraction: Double): String {
        require(!(fraction < 0 || fraction > 1)) { "Fraction provided that is not within 0 and 1: $fraction" }
        return PERCENTAGE_FORMAT.format(fraction * 100)
    }
}