package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object EligibleExternalTrialGeneratorFunctions {

    fun dutchTrials(externalTrialsPerEvent: Map<String, List<ExternalTrial>>): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { it.countries.contains(Country.NETHERLANDS) }
    }

    fun nonDutchTrials(externalTrialsPerEvent: Map<String, List<ExternalTrial>>): Map<String, List<ExternalTrial>> {
        return filterMapOfExternalTrials(externalTrialsPerEvent) { !it.countries.contains(Country.NETHERLANDS) }
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

    private fun filterMapOfExternalTrials(
        externalTrialsPerEvent: Map<String, List<ExternalTrial>>, filter: (ExternalTrial) -> Boolean
    ): Map<String, List<ExternalTrial>> {
        return externalTrialsPerEvent.mapValues { (_, externalTrials) -> externalTrials.filter(filter::invoke) }
            .filterValues { it.isNotEmpty() }
    }
}