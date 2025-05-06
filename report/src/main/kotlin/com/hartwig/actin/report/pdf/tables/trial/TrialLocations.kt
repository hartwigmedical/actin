package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohortFunctions
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.trial.ExternalTrialSummary

const val MAX_TO_DISPLAY = 3
const val MANY_LOCATIONS = "$MAX_TO_DISPLAY+ locations"
const val SEE_LINK = " - see link"

object TrialLocations {

    fun actinTrialLocation(trialSource: TrialSource?, requestingSource: TrialSource?, locations: Set<String>, showLinks: Boolean): String {
        val showRequestingSite = requestingSource != null &&
                InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(trialSource, locations, requestingSource)

        return when {
            showRequestingSite && locations.size > MAX_TO_DISPLAY - 1 -> {
                val otherLocationCount = locations.size - 1
                "${requestingSource?.description} and $otherLocationCount other locations${if (showLinks) SEE_LINK else ""}"
            }

            locations.size > MAX_TO_DISPLAY -> "$MANY_LOCATIONS${ if (showLinks) SEE_LINK else "" }"

            else -> locations.sorted().joinToString(Formats.COMMA_SEPARATOR)
        }
    }

    fun externalTrialLocation(trial: ExternalTrialSummary, countryOfReference: Country?): String {
        return countryOfReference?.let {
            val (hospitals, cities) = ExternalTrialFunctions.hospitalsAndCitiesInCountry(trial, it)
            if (countryOfReference == Country.NETHERLANDS && hospitals != MANY_LOCATIONS) hospitals else cities
        } ?: ExternalTrialFunctions.countryNamesWithCities(trial)
    }
}