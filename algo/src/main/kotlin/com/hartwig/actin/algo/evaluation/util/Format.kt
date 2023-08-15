package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.Displayable
import com.hartwig.actin.util.ApplicationConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Format {
    private const val SEPARATOR_SEMICOLON = "; "
    private const val SEPARATOR_AND = " and "
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val PERCENTAGE_FORMAT: DecimalFormat = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(ApplicationConfig.LOCALE))

    fun concat(strings: Iterable<String>): String {
        return concatStrings(strings, SEPARATOR_SEMICOLON)
    }

    fun concatItems(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_SEMICOLON)
    }

    fun concatLowercaseWithAnd(strings: Iterable<String>): String {
        return concatStrings(strings.map(String::lowercase), SEPARATOR_AND)
    }

    fun concatItemsWithAnd(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_AND)
    }

    fun date(date: LocalDate): String {
        return DATE_FORMAT.format(date)
    }

    fun percentage(fraction: Double): String {
        require(!(fraction < 0 || fraction > 1)) { "Fraction provided that is not within 0 and 1: $fraction" }
        return PERCENTAGE_FORMAT.format(fraction * 100)
    }

    private fun concatDisplayables(items: Iterable<Displayable>, separator: String) =
        concatStrings(items.map(Displayable::display), separator)

    private fun concatStrings(strings: Iterable<String>, separator: String) =
        strings.distinct().sorted().joinToString(separator)
}