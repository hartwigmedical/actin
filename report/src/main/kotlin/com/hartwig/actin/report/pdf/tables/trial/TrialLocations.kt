package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohortFunctions
import com.hartwig.actin.report.pdf.util.Formats

const val MAX_TO_DISPLAY = 3
const val MANY_SEE_LINK = "3+ locations - see link"

object TrialLocations {

    fun concat(source: TrialSource?, requestingSource: TrialSource?, locations: Set<String>): String {
        val showRequestingSite = requestingSource != null &&
                InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(source, locations, requestingSource)

        return when {
            showRequestingSite && locations.size > MAX_TO_DISPLAY - 1 -> {
                val otherLocationCount = locations.size - 1
                "${requestingSource?.description} and $otherLocationCount other locations - see link"
            }

            locations.size > MAX_TO_DISPLAY -> MANY_SEE_LINK

            else -> locations.sorted().joinToString(Formats.COMMA_SEPARATOR)
        }
    }
}