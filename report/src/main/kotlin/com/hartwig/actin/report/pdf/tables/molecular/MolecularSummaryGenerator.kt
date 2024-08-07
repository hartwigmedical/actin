package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.LogManager

class MolecularSummaryGenerator(
    private val patientRecord: PatientRecord,
    private val cohorts: List<EvaluatedCohort>,
    private val keyWidth: Float, private val valueWidth: Float,
    private val isShort: Boolean
) : TableGenerator {
    override fun title(): String {
        return "Recent molecular results"
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(keyWidth + valueWidth)
        val latestOrange = patientRecord.molecularHistory.latestOrangeMolecularRecord()

        if (latestOrange != null && latestOrange.hasSufficientQuality) {
            if (latestOrange.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
                LOGGER.warn("Generating WGS results for non-WGS sample")
            }
            val wgsGenerator = WGSSummaryGenerator(isShort, patientRecord, latestOrange, cohorts, keyWidth, valueWidth)
            table.addCell(Cells.createSubTitle(wgsGenerator.title()))
            table.addCell(Cells.create(wgsGenerator.contents()))
        } else {
            val noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth)
            noRecent.addCell(Cells.createKey(latestOrange?.experimentType?.display() + " results"))
            noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"))
            table.addCell(Cells.create(noRecent))
        }

        val priorMolecularResultGenerator =
            PriorMolecularResultGenerator(patientRecord, keyWidth, valueWidth, PriorMolecularTestInterpreter())
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.create(priorMolecularResultGenerator.contents()))
        return table
    }

    companion object {
        private val LOGGER = LogManager.getLogger(MolecularSummaryGenerator::class.java)
    }
}