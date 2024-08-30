package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object EligibleExternalTrialGeneratorFunctions {

    fun localTrials(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, homeCountry: CountryName): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { it.countries.map { country -> country.countryName }.contains(homeCountry) }
    }

    fun nonLocalTrials(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
        homeCountry: CountryName
    ): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { !it.countries.map { country -> country.countryName }.contains(homeCountry) }
    }

    fun shortenTitle(title: String): String {
        return if (title.length > 170) {
            title.take(85).substringBeforeLast(" ") + " ... " + title.takeLast(85).substringAfter(" ")
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

    fun hospitalsInHomeCountry(externalTrial: ExternalTrial, homeCountryName: CountryName): List<String> {
        val homeCountry = externalTrial.countries.filter { it.countryName == homeCountryName }
        return if (homeCountry.size > 1) {
            throw IllegalStateException("Home country ${homeCountryName.display()} found multiple times")
        } else {
            val hospitals = homeCountry.first().hospitalsPerCity.flatMap { it.value }
            if (hospitals.size > 10) {
                (listOf("Many (please check clinicaltrials.gov link)"))
            } else hospitals
        }
    }

    fun countryNamesAndCities(externalTrial: ExternalTrial): String {
        return externalTrial.countries.joinToString { country ->
            val cities = if (country.hospitalsPerCity.keys.size > 8) {
                "Many (please check clinicaltrials.gov link)"
            } else {
                country.hospitalsPerCity.keys.joinToString(", ")
            }
            "${country.countryName.display()} ($cities)"
        }
    }

    private fun filterMapOfExternalTrials(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, filter: (ExternalTrial) -> Boolean
    ): Map<String, List<ExternalTrial>> {
        return externalTrialsPerEvent.mapValues { (_, externalTrials) -> externalTrials.filter(filter::invoke) }
            .filterValues { it.isNotEmpty() }
    }


}