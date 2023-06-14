package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.datamodel.Gender
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

class FeedLine(private val fields: Map<String, Int>, private val parts: Array<String>) {
    fun string(column: String): String {
        require(fields.containsKey(column)) { "No column found with header '$column'" }
        val string = parts[fields[column]!!]
        return if (string != NULL_STRING) string else Strings.EMPTY
    }

    fun hasColumn(column: String): Boolean {
        return fields.containsKey(column)
    }

    fun trimmed(column: String): String {
        return string(column).trim { it <= ' ' }
    }

    fun gender(column: String): Gender {
        return FeedParseFunctions.parseGender(string(column))
    }

    fun date(column: String): LocalDate {
        return FeedParseFunctions.parseDate(string(column))
    }

    fun optionalDate(column: String): LocalDate? {
        return FeedParseFunctions.parseOptionalDate(string(column))
    }

    fun number(column: String): Double {
        return FeedParseFunctions.parseDouble(string(column))
    }

    fun optionalNumber(column: String): Double? {
        return FeedParseFunctions.parseOptionalDouble(string(column))
    }

    fun integer(column: String): Int {
        return string(column).toInt()
    }

    companion object {
        const val NULL_STRING = "NULL"
    }
}