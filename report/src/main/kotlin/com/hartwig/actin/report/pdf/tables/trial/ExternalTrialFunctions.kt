package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.report.trial.ExternalTrialSummary

private const val MAX_TO_DISPLAY = 3
const val MANY_PLEASE_CHECK_LINK = ">3 locations - please check link"

object ExternalTrialFunctions {

    fun hospitalsAndCitiesInCountry(trial: ExternalTrialSummary, country: Country): Pair<String, String> {
        val homeCountries = trial.countries.filter { it.country == country }
        
        return if (homeCountries.size > 1 || homeCountries.isEmpty()) {
            throw IllegalStateException(
                "Country ${country.display()} not configured or configured multiple times for trial ${trial.nctId}. " +
                        "This should not be possible and indicates an issue in the SERVE data export"
            )
        } else {
            val (cities, hospitals) = homeCountries.first().hospitalsPerCity
                .let { listOf(it.keys, it.values.flatten().map(Hospital::name)) }
                .map { if (it.size > MAX_TO_DISPLAY) MANY_PLEASE_CHECK_LINK else it.joinToString() }
            hospitals to cities
        }
    }

    fun countryNamesWithCities(externalTrial: ExternalTrialSummary): String {
        return externalTrial.countries.joinToString { country ->
            val cities = if (country.hospitalsPerCity.keys.size > MAX_TO_DISPLAY) {
                MANY_PLEASE_CHECK_LINK
            } else {
                country.hospitalsPerCity.keys.joinToString(", ")
            }
            "${country.country.display()} ($cities)"
        }
    }
}