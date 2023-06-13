package com.hartwig.actin.clinical.feed

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.Gender
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object FeedParseFunctions {
    private val DATE_FORMATS: MutableSet<DateTimeFormatter> = Sets.newHashSet()

    init {
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"))
    }

    @JvmStatic
    fun parseGender(gender: String): Gender {
        if (gender.equals("male", ignoreCase = true)) {
            return Gender.MALE
        } else if (gender.equals("female", ignoreCase = true)) {
            return Gender.FEMALE
        }
        throw IllegalArgumentException("Could not resolve gender: $gender")
    }

    @JvmStatic
    fun parseOptionalDate(date: String): LocalDate? {
        return if (!date.isEmpty()) parseDate(date) else null
    }

    @JvmStatic
    fun parseDate(date: String): LocalDate {
        for (format in DATE_FORMATS) {
            if (canBeInterpretedWithFormat(date, format)) {
                return LocalDate.parse(date, format)
            }
        }
        throw IllegalArgumentException("Cannot transform string to date using any of the configured date formats: $date")
    }

    private fun canBeInterpretedWithFormat(date: String, format: DateTimeFormatter): Boolean {
        return try {
            LocalDate.parse(date, format)
            true
        } catch (exception: DateTimeParseException) {
            false
        }
    }

    @JvmStatic
    fun parseOptionalDouble(number: String): Double? {
        return if (!number.isEmpty()) parseDouble(number) else null
    }

    fun parseDouble(number: String): Double {
        val formatted = number.replace(",", ".")
        require(formatted.indexOf(".") == formatted.lastIndexOf(".")) { "Cannot convert feed value to number: $number" }
        return formatted.toDouble()
    }
}