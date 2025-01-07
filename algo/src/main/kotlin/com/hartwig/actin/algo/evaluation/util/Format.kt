package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.util.ApplicationConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {

    private const val SEPARATOR_AND = " and "
    private const val SEPARATOR_OR = " or "
    private const val SEPARATOR_COMMA = ", "
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val PERCENTAGE_FORMAT: DecimalFormat = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(ApplicationConfig.LOCALE))

    fun concatWithCommaAndOr(strings: Iterable<String>) = concatWithCommaAndSeparator(strings, SEPARATOR_OR, toLowercase = false)

    fun concatWithCommaAndAnd(strings: Iterable<String>) = concatWithCommaAndSeparator(strings, SEPARATOR_AND, toLowercase = false)

    fun concatLowercaseWithAnd(strings: Iterable<String>): String {
        return concatStrings(strings.map(String::lowercase), SEPARATOR_AND)
    }

    fun concatLowercaseUnlessNumericWithAnd(strings: Iterable<String>): String {
        return concatStrings(strings.map { if (it.any(Char::isDigit)) it else it.lowercase() }, SEPARATOR_AND)
    }

    fun concatLowercaseWithCommaAndOr(strings: Iterable<String>) = concatWithCommaAndSeparator(strings, SEPARATOR_OR, toLowercase = true)

    fun concatLowercaseWithCommaAndAnd(strings: Iterable<String>) = concatWithCommaAndSeparator(strings, SEPARATOR_AND, toLowercase = true)

    private fun concatWithCommaAndSeparator(strings: Iterable<String>, separator: String, toLowercase: Boolean): String {
        val stringList = if (toLowercase) strings.distinct().map(String::lowercase) else strings.distinct()
        val sortedStringList = stringList.sortedWith(String.CASE_INSENSITIVE_ORDER)
        return if (sortedStringList.size < 2) {
            concatStrings(sortedStringList, SEPARATOR_COMMA)
        } else {
            listOf(sortedStringList.dropLast(1).joinToString(", "), sortedStringList.last()).joinToString(separator)
        }
    }

    fun concatItemsWithAnd(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_AND)
    }

    fun concatItemsWithOr(items: Iterable<Displayable>): String {
        return concatDisplayables(items, SEPARATOR_OR)
    }

    fun date(date: LocalDate): String {
        return DATE_FORMAT.format(date)
    }

    fun percentage(fraction: Double): String {
        require(!(fraction < 0 || fraction > 1)) { "Fraction provided that is not within 0 and 1: $fraction" }
        return PERCENTAGE_FORMAT.format(fraction * 100)
    }

    fun labReference(factorValue: Double, factorUnit: String, refLimit: Double?): String {
        val result = refLimit?.let { String.format(Locale.ENGLISH, "%.1f", factorValue * refLimit) } ?: "$factorValue*NA"
        return "$factorValue*${factorUnit} ($result)"
    }

    fun labValue(labMeasurement: LabMeasurement, value: Double, unit: LabUnit): String {
        val formattedValue = String.format(Locale.ENGLISH, "%.1f", value)
        return "${labMeasurement.display().replaceFirstChar { it.uppercase() }} $formattedValue ${unit.display()}"
    }

    private fun concatDisplayables(items: Iterable<Displayable>, separator: String) =
        concatStrings(items.map(Displayable::display), separator)

    private fun concatStrings(strings: Iterable<String>, separator: String) =
        strings.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER).joinToString(separator)
}