package com.hartwig.actin.report.pdf.tables.clinical

object DateFunctions {

    fun toDateString(maybeYear: Int?, maybeMonth: Int?, maybeDay: Int? = null): String? =
        when {
            maybeYear == null -> null
            maybeMonth == null -> "%04d".format(maybeYear)
            maybeDay == null -> "%04d-%02d".format(maybeYear, maybeMonth)
            else -> "%04d-%02d-%02d".format(maybeYear, maybeMonth, maybeDay)
        }
}