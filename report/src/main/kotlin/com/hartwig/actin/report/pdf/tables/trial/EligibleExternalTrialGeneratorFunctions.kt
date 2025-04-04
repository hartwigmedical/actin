package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.report.trial.ExternalTrialSummary

private const val MANY_PLEASE_CHECK_LINK = "Many, please check link"

object EligibleExternalTrialGeneratorFunctions {
    
    fun shortenTitle(title: String): String {
        return if (title.length > 160) {
            title.take(80).substringBeforeLast(" ") + " ... " + title.takeLast(80).substringAfter(" ")
        } else {
            title
        }
    }

    fun hospitalsAndCitiesInCountry(trial: ExternalTrialSummary, country: Country): Pair<String, String> {
        val homeCountries = trial.countries.filter { it.country == country }
        
        return if (homeCountries.size > 1 || homeCountries.isEmpty()) {
            throw IllegalStateException(
                "Country ${country.display()} not configured or configured multiple times for trial ${trial.nctId}. " +
                        "This should not be possible and indicates an issue in the SERVE data export"
            )
        } else {
            val hospitals = homeCountries.first().hospitalsPerCity.flatMap { it.value }
            val cities = homeCountries.first().hospitalsPerCity.keys
            val hospitalsString = if (hospitals.size > 10) {
                MANY_PLEASE_CHECK_LINK
            } else hospitals.joinToString { it.name }
            val citiesString = if (cities.size > 8) {
                MANY_PLEASE_CHECK_LINK
            } else cities.joinToString { it }
            Pair(hospitalsString, citiesString)
        }
    }

    fun countryNamesWithCities(externalTrial: ExternalTrialSummary): String {
        return externalTrial.countries.joinToString { country ->
            val cities = if (country.hospitalsPerCity.keys.size > 8) {
                MANY_PLEASE_CHECK_LINK
            } else {
                country.hospitalsPerCity.keys.joinToString(", ")
            }
            "${country.country.display()} ($cities)"
        }
    }
}