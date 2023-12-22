package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object EligibleExternalTrialGeneratorFunctions {

    fun dutchTrials(externalTrialsPerEvent: Multimap<String, ExternalTrial>): Multimap<String, ExternalTrial> {
        val dutchTrials = ArrayListMultimap.create<String, ExternalTrial>()
        externalTrialsPerEvent.forEach { event, eligibleTrial ->
            if (eligibleTrial.countries().contains("Netherlands")) {
                dutchTrials.put(event, eligibleTrial)
            }
        }
        return dutchTrials
    }

    fun nonDutchTrials(externalTrialsPerEvent: Multimap<String, ExternalTrial>): Multimap<String, ExternalTrial> {
        val nonDutchTrials = ArrayListMultimap.create<String, ExternalTrial>()
        externalTrialsPerEvent.forEach { event, eligibleTrial ->
            if (!eligibleTrial.countries().contains("Netherlands")) {
                nonDutchTrials.put(event, eligibleTrial)
            }
        }
        return nonDutchTrials
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
}