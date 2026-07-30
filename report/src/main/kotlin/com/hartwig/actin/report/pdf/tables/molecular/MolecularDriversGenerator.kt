package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.molecular.interpretation.ClonalityInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortsSummarizer
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.interpretation.TrialAcronymAndLocations
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialLocations
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ActionableWithExternalTrial
import com.itextpdf.layout.element.Table

class MolecularDriversGenerator(
    private val molecular: MolecularTest,
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ActionableWithExternalTrial>,
    private val title: String,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(35f, 21f, 10f, 10f, 11f, 10f)

        table.addHeaderCell(Cells.createHeader(labels.molecular.colType()))
        table.addHeaderCell(Cells.createHeader(labels.molecular.colDriver()))
        table.addHeaderCell(Cells.createHeader(labels.molecular.colTrialsLocations()))
        table.addHeaderCell(Cells.createHeader(labels.molecular.colTrialsSource(molecular.externalTrialSource)))
        table.addHeaderCell(Cells.createHeader(labels.molecular.colBestEvidence(molecular.evidenceSource)))
        table.addHeaderCell(Cells.createHeader(labels.molecular.colResistance(molecular.evidenceSource)))

        val molecularDriversInterpreter = MolecularDriversInterpreter(molecular.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        val externalTrialsPerSingleEvent = DriverTableFunctions.groupByEvent(externalTrials)
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        factory.create().forEach { entry ->
            table.addCell(Cells.createContent(entry.driverType))
            table.addCell(Cells.createContent(entry.display()))
            table.addCell(Cells.createContent(formatActinTrials(entry.actinTrials)))
            table.addCell(Cells.createContent(externalTrialsPerSingleEvent[entry.event] ?: ""))
            table.addCell(Cells.createContent(entry.bestResponsiveEvidence ?: ""))
            table.addCell(Cells.createContent(entry.bestResistanceEvidence ?: ""))
        }
        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            val note = labels.molecular.subClonalNote(Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF))
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
                        emptySet(),
                        null,
                        it.locations,
                        false
                    )
                })" else ""
            }"
        }
    }
}
