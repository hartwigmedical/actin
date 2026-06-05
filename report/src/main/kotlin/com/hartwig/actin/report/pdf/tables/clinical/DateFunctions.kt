package com.hartwig.actin.report.pdf.tables.clinical

object DateFunctions {

    fun toDateString(year: Int?, month: Int?, day: Int? = null): String? =
        when {
            year == null -> null
            month == null -> "%04d".format(year)
            day == null -> "%04d-%02d".format(year, month)
            else -> "%04d-%02d-%02d".format(year, month, day)
        }
}