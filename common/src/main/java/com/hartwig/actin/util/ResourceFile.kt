package com.hartwig.actin.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ResourceFile {
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val UNKNOWN: String = "unknown"

    @JvmStatic
    fun optionalString(string: String): String? {
        return if (hasValue(string)) string else null
    }

    @JvmStatic
    fun optionalBool(bool: String): Boolean? {
        return if (hasValue(bool)) bool(bool) else null
    }

    @JvmStatic
    fun bool(bool: String): Boolean {
        if ((bool == "1")) {
            return true
        } else if ((bool == "0")) {
            return false
        } else {
            throw IllegalArgumentException("Cannot convert curation value to boolean: '" + bool + "'")
        }
    }

    @JvmStatic
    fun optionalDate(date: String): LocalDate? {
        return if (hasValue(date)) date(date) else null
    }

    fun date(date: String): LocalDate {
        return LocalDate.parse(date, DATE_FORMAT)
    }

    @JvmStatic
    fun optionalInteger(integer: String): Int? {
        return if (hasValue(integer)) integer(integer) else null
    }

    fun integer(integer: String): Int {
        return integer.toInt()
    }

    @JvmStatic
    fun optionalNumber(doubleString: String): Double? {
        return if (hasValue(doubleString)) number(doubleString) else null
    }

    fun number(doubleString: String): Double {
        return doubleString.toDouble()
    }

    private fun hasValue(string: String): Boolean {
        return !string.isEmpty() && !(string == UNKNOWN)
    }
}
