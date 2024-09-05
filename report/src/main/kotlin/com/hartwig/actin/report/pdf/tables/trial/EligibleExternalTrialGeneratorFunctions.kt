package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

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

    fun hospitalsAndCitiesInCountry(externalTrial: ExternalTrial, country: CountryName): Pair<String, String> {
        val homeCountries = externalTrial.countries.filter { it.name == country }
        return if (homeCountries.size > 1 || homeCountries.isEmpty()) {
            throw IllegalStateException("Country ${country.display()} not found or found multiple times")
        } else {
            val hospitals = homeCountries.first().hospitalsPerCity.flatMap { it.value }
            val cities = homeCountries.first().hospitalsPerCity.keys
            val hospitalsString = if (hospitals.size > 10) {
                "Many (please check link)"
            } else hospitals.joinToString { it }
            val citiesString = if (cities.size > 8) {
                "Many (please check link)"
            } else cities.joinToString { it }
            Pair(hospitalsString, citiesString)
        }
    }

    fun countryNamesWithCities(externalTrial: ExternalTrial): String {
        return externalTrial.countries.joinToString { country ->
            val cities = if (country.hospitalsPerCity.keys.size > 8) {
                "Many (please check link)"
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