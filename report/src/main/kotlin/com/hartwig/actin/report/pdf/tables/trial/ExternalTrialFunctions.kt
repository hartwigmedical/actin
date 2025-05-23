package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.Hospital

object ExternalTrialFunctions {

    private val format: (Collection<String>) -> String = { items ->
        if (items.size > MAX_TO_DISPLAY) MANY_LOCATIONS_SEE_LINK else items.joinToString()
    }

    private fun formatHospitalsAndCities(hospitalsPerCity: Map<String, Set<Hospital>>): Pair<String, String> {
        val cities = hospitalsPerCity.keys
        val hospitals = hospitalsPerCity.values.flatten().map(Hospital::name)
        return format(hospitals) to format(cities)
    }

    fun hospitalsAndCitiesInCountry(trial: ExternalTrialSummary, country: Country): Pair<String, String> {
        val matchingCountry = trial.countries.filter { it.country == country }

        if (matchingCountry.size != 1) {
            throw IllegalStateException(
                "Country ${country.display()} not configured or configured multiple times for trial ${trial.nctId}. " +
                        "This should not be possible and indicates an issue in the SERVE data export"
            )
        }

        return formatHospitalsAndCities(matchingCountry.first().hospitalsPerCity)
    }

    fun countryNamesWithCities(externalTrial: ExternalTrialSummary): String {
        return externalTrial.countries.joinToString { country ->
            val (_, cities) = formatHospitalsAndCities(country.hospitalsPerCity)
            "${country.country.display()} ($cities)"
        }
    }
}