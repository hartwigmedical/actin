package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.report.interpretation.ClonalityInterpreter
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.interpretation.TrialAcronymAndLocations
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialLocations
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.EventWithExternalTrial
import com.itextpdf.layout.element.Table

class MolecularDriversGenerator(
    private val molecularDriversInterpreter: MolecularDriversInterpreter,
    private val externalTrials: Set<EventWithExternalTrial>,
    private val externalTrialSource: String,
    private val evidenceSource: String,
    private val title: String
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(35f, 21f, 10f, 10f, 11f, 10f)

        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Driver"))
        table.addHeaderCell(Cells.createHeader("Trials (Locations)"))
        table.addHeaderCell(Cells.createHeader("Trials in $externalTrialSource"))
        table.addHeaderCell(Cells.createHeader("Best evidence in $evidenceSource"))
        table.addHeaderCell(Cells.createHeader("Resistance in $evidenceSource"))

        val externalTrialsPerSingleEvent = DriverTableFunctions.groupByEvent(externalTrials)
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        factory.create().sortedWith(driverSortOrder).forEach { entry ->
            table.addCell(Cells.createContent(entry.driverType))
            table.addCell(Cells.createContent(entry.display()))
            table.addCell(Cells.createContent(formatActinTrials(entry.actinTrials)))
            table.addCell(Cells.createContent(externalTrialsPerSingleEvent[entry.event] ?: ""))
            table.addCell(Cells.createContent(entry.bestResponsiveEvidence ?: ""))
            table.addCell(Cells.createContent(entry.bestResistanceEvidence ?: ""))
        }
        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            val note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal"
            table.addCell(Cells.createSpanningSubNote(note, table))
        }
        return table
    }

    private fun formatActinTrials(actinTrials: Set<TrialAcronymAndLocations>): String {
        return actinTrials.joinToString(", ")
        {
            "${it.trialAcronym} ${
                if (it.locations.isNotEmpty()) "(${
                    TrialLocations.actinTrialLocation(
                        null,
                        null,
                        it.locations,
                        false
                    )
                })" else ""
            }"
        }
    }

    private val driverSortOrder: Comparator<MolecularDriverEntry> = compareBy(
        MolecularDriverEntry::driverType,
        MolecularDriverEntry::description
    )
}