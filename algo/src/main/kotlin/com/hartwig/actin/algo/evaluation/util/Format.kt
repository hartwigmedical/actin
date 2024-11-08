package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.util.ApplicationConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {
    private const val SEPARATOR_SEMICOLON = "; "
    private const val SEPARATOR_AND = " and "
    private const val SEPARATOR_OR = " or "
    private const val SEPARATOR_COMMA = ", "
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

    fun concatStringsWithAnd(strings: Iterable<String>): String {
        return concatStrings(strings, SEPARATOR_AND)
    }

    fun concatLowercaseWithCommaAndOr(strings: Iterable<String>): String {
        val stringList = strings.map(String::lowercase).distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
        return if (stringList.size < 2) {
            concat(stringList)
        } else {
            listOf(stringList.dropLast(1).joinToString(", "), stringList.last()).joinToString(SEPARATOR_OR)
        }
    }

    fun concatWithCommaAndAnd(strings: Iterable<String>): String {
        val stringList = strings.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
        return if (stringList.size < 2) {
            concat(stringList)
        } else {
            listOf(stringList.dropLast(1).joinToString(", "), stringList.last()).joinToString(SEPARATOR_AND)
        }
    }

    fun concatItemsWithAnd(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_AND)
    }

    fun concatItemsWithOr(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_OR)
    }

    fun concatItemsWithComma(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_COMMA)
    }

    fun date(date: LocalDate): String {
        return DATE_FORMAT.format(date)
    }

    fun percentage(fraction: Double): String {
        require(!(fraction < 0 || fraction > 1)) { "Fraction provided that is not within 0 and 1: $fraction" }
        return PERCENTAGE_FORMAT.format(fraction * 100)
    }

    fun labReference(factorValue: Double, factorUnit: String, refLimit: Double?): String {
        val formattedRefLimit = refLimit?.let { String.format(Locale.ENGLISH, "%.1f", it) } ?: "NA"
        return "$factorValue*${factorUnit} ($factorValue*$formattedRefLimit)"
    }

    fun labValue(labMeasurement: LabMeasurement, value: Double): String {
        return "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${String.format(Locale.ENGLISH, "%.1f", value)}"
    }

    private fun concatDisplayables(items: Iterable<Displayable>, separator: String) =
        concatStrings(items.map(Displayable::display), separator)

    private fun concatStrings(strings: Iterable<String>, separator: String) =
        strings.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER).joinToString(separator)
}