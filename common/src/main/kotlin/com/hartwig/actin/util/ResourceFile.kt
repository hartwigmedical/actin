package com.hartwig.actin.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ResourceFile {

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private const val UNKNOWN: String = "unknown"

    fun optionalString(string: String): String? {
        return if (hasValue(string)) string else null
    }

    fun optionalBool(bool: String): Boolean? {
        return if (hasValue(bool)) bool(bool) else null
    }

    fun bool(bool: String): Boolean {
        return when (bool) {
            "1" -> {
                true
            }
            "0" -> {
                false
            }
            else -> {
                throw IllegalArgumentException("Cannot convert curation value to boolean: '$bool'")
            }
        }
    }

    fun optionalDate(date: String): LocalDate? {
        return if (hasValue(date)) date(date) else null
    }

    fun date(date: String): LocalDate {
        return LocalDate.parse(date, DATE_FORMAT)
    }

    fun optionalInteger(integer: String): Int? {
        return if (hasValue(integer)) integer(integer) else null
    }

    fun integer(integer: String): Int {
        return integer.toInt()
    }

    fun optionalNumber(doubleString: String): Double? {
        return if (hasValue(doubleString)) number(doubleString) else null
    }

    fun number(doubleString: String): Double {
        return doubleString.toDouble()
    }

    private fun hasValue(string: String): Boolean {
        return string.isNotEmpty() && string != UNKNOWN
    }
}
