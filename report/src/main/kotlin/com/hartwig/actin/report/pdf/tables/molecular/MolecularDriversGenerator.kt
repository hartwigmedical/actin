package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.report.interpretation.ClonalityInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortsSummarizer
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.layout.element.Table

class MolecularDriversGenerator(
    private val molecular: MolecularRecord,
    private val cohorts: List<InterpretedCohort>,
    private val externalTrials: Set<ExternalTrialSummary>
) : TableGenerator {

    override fun title(): String {
        return "Drivers"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 2f, 1f, 1f, 1f, 1f, 1f)

        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Driver"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))
        table.addHeaderCell(Cells.createHeader("Trials (Locations)"))
        table.addHeaderCell(Cells.createHeader("Trials in ${molecular.externalTrialSource}"))
        table.addHeaderCell(Cells.createHeader("Best evidence in ${molecular.evidenceSource}"))
        table.addHeaderCell(Cells.createHeader("Resistance in ${molecular.evidenceSource}"))

        val molecularDriversInterpreter = MolecularDriversInterpreter(molecular.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        val externalTrialsPerSingleEvent = DriverTableFunctions.groupByEvent(externalTrials)
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        factory.create().forEach { entry: MolecularDriverEntry ->
            table.addCell(Cells.createContent(entry.driverType))
            table.addCell(Cells.createContent(entry.display()))
            table.addCell(Cells.createContent(formatDriverLikelihood(entry.driverLikelihood)))
            table.addCell(
                Cells.createContent(entry.actinTrials.joinToString(", ")
                { "${it.trialAcronym} ${if (it.locations.isNotEmpty()) "(${it.locations.joinToString()})" else ""}" })
            )
            table.addCell(Cells.createContent(externalTrialsPerSingleEvent[entry.event]?.let { concatEligibleTrials(it) } ?: ""))
            table.addCell(Cells.createContent(entry.bestResponsiveEvidence ?: ""))
            table.addCell(Cells.createContent(entry.bestResistanceEvidence ?: ""))
        }
        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            val note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal"
            table.addCell(Cells.createSpanningSubNote(note, table))
        }
        return table
    }

    private fun formatDriverLikelihood(driverLikelihood: DriverLikelihood?): String {
        return driverLikelihood?.let(DriverLikelihood::toString) ?: Formats.VALUE_UNKNOWN
    }

    private fun concatEligibleTrials(externalTrials: Iterable<ExternalTrialSummary>): String {
        return externalTrials.map { it.nctId }.toSet().joinToString(", ")
    }
}