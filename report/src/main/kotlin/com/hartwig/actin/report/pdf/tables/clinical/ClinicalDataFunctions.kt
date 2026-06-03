package com.hartwig.actin.report.pdf.tables.clinical

object ClinicalDataFunctions {

    fun toDateString(maybeYear: Int?, maybeMonth: Int?, maybeDay: Int? = null): String? =
        when {
            maybeYear == null -> null
            maybeMonth == null -> "$maybeYear"
            maybeDay == null -> "$maybeYear-$maybeMonth"
            else -> "$maybeYear-$maybeMonth-$maybeDay"
        }
}