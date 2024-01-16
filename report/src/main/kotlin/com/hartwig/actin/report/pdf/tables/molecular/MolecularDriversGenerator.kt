package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.ClonalityInterpreter
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortsInterpreter
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.hartwig.actin.trial.TreatmentConstants
import com.itextpdf.layout.element.Table

class MolecularDriversGenerator(
    private val molecular: MolecularRecord, private val cohorts: List<EvaluatedCohort>, private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Drivers"
    }

    override fun contents(): Table {
        val colWidth = width / 8
        val table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth)
        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Driver"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))
        table.addHeaderCell(Cells.createHeader("Trials in " + TreatmentConstants.ACTIN_SOURCE))
        table.addHeaderCell(Cells.createHeader("Trials in " + molecular.externalTrialSource()))
        table.addHeaderCell(Cells.createHeader("Best evidence in " + molecular.evidenceSource()))
        table.addHeaderCell(Cells.createHeader("Resistance in " + molecular.evidenceSource()))

        val molecularDriversInterpreter =
            MolecularDriversInterpreter(molecular.drivers(), EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        factory.create().forEach { entry: MolecularDriverEntry ->
            table.addCell(Cells.createContent(entry.driverType))
            table.addCell(Cells.createContent(entry.driver))
            table.addCell(Cells.createContent(formatDriverLikelihood(entry.driverLikelihood)))
            table.addCell(Cells.createContent(concat(entry.actinTrials)))
            table.addCell(Cells.createContent(concatEligibleTrials(entry.externalTrials)))
            table.addCell(Cells.createContent(entry.bestResponsiveEvidence ?: ""))
            table.addCell(Cells.createContent(entry.bestResistanceEvidence ?: ""))
        }
        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            val note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal"
            table.addCell(Cells.createSpanningSubNote(note, table))
        }
        return makeWrapping(table)
    }

    companion object {
        private fun formatDriverLikelihood(driverLikelihood: DriverLikelihood?): String {
            return driverLikelihood?.let(DriverLikelihood::toString) ?: Formats.VALUE_UNKNOWN
        }

        private fun concat(treatments: Set<String>): String {
            return treatments.joinToString(", ")
        }

        private fun concatEligibleTrials(externalTrials: Set<ExternalTrial>): String {
            val strings = mutableSetOf<String>()
            for (externalTrial in externalTrials) {
                strings.add(externalTrial.nctId())
            }
            return strings.joinToString(", ")
        }

    }
}