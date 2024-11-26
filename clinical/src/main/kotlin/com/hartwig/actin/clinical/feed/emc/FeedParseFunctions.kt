package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.Gender
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object FeedParseFunctions {
    private val DATE_FORMATS: MutableSet<DateTimeFormatter> = mutableSetOf()

    init {
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"))
    }

    fun parseGender(gender: String): Gender {
        return when {
            gender.equals("male", ignoreCase = true) -> Gender.MALE

            gender.equals("female", ignoreCase = true) -> Gender.FEMALE

            else -> throw IllegalArgumentException("Could not resolve gender: $gender")
        }
    }

    fun parseOptionalDate(date: String): LocalDate? {
        return if (date.isNotEmpty()) parseDate(date) else null
    }

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
            LocalDateTime.parse(date, format)
            true
        } catch (exception: DateTimeParseException) {
            false
        }
    }

    fun parseOptionalDouble(number: String): Double? {
        return if (number.isNotEmpty()) parseDouble(number) else null
    }

    fun parseDouble(number: String): Double {
        val formatted = number.replace(",", ".")
        require(formatted.indexOf(".") == formatted.lastIndexOf(".")) { "Cannot convert feed value to number: $number" }
        return formatted.toDouble()
    }
}