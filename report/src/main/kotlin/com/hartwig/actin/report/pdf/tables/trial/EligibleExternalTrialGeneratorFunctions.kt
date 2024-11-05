package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

private const val MANY_PLEASE_CHECK_LINK = "Many, please check link"

object EligibleExternalTrialGeneratorFunctions {

    fun localTrials(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
        homeCountry: CountryName
    ): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { it.countries.map { country -> country.name }.contains(homeCountry) }
    }

    fun nonLocalTrials(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
        homeCountry: CountryName
    ): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { !it.countries.map { country -> country.name }.contains(homeCountry) }
    }

    fun shortenTitle(title: String): String {
        return if (title.length > 160) {
            title.take(80).substringBeforeLast(" ") + " ... " + title.takeLast(80).substringAfter(" ")
        } else {
            title
        }
    }

    fun insertRow(table: Table, subTable: Table) {
        val finalSubTable = if (subTable.numberOfRows > 2) {
            Tables.makeWrapping(subTable, false)
        } else {
            subTable.setKeepTogether(true)
        }
        table.addCell(Cells.createContent(finalSubTable))
    }

    fun hospitalsAndCitiesInCountry(trial: ExternalTrialSummary, country: CountryName): Pair<String, String> {
        val homeCountries = trial.countries.filter { it.name == country }
        return if (homeCountries.size > 1 || homeCountries.isEmpty()) {
            throw IllegalStateException(
                "Country ${country.display()} not configured or configured multiple times for trial ${trial.nctId}. " +
                        "This should not be possible and indicates an issue in the SERVE data export"
            )
        } else {
            val hospitalsString = if (trial.hospitals.size > 10) {
                MANY_PLEASE_CHECK_LINK
            } else trial.hospitals.joinToString { it }
            val citiesString = if (trial.cities.size > 8) {
                MANY_PLEASE_CHECK_LINK
            } else trial.cities.joinToString { it }
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
            "${country.name.display()} ($cities)"
        }
    }

    private fun filterMapOfExternalTrials(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, filter: (ExternalTrial) -> Boolean
    ): Map<String, List<ExternalTrial>> {
        return externalTrialsPerEvent.mapValues { (_, externalTrials) -> externalTrials.filter(filter::invoke) }
            .filterValues { it.isNotEmpty() }
    }
}