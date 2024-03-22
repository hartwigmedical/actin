package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.LogManager

class MolecularSummaryGenerator(
    private val clinical: ClinicalRecord, private val molecularHistory: MolecularHistory,
    private val cohorts: List<EvaluatedCohort>, private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    override fun title(): String {
        return "Recent molecular results"
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(keyWidth + valueWidth)
        val molecular = molecularHistory.mostRecentWGS()!!  // TODO kz fix non-null
        if (molecular.containsTumorCells) {
            if (molecular.type != ExperimentType.WHOLE_GENOME) {
                LOGGER.warn("Generating WGS results for non-WGS sample")
            }
            val wgsGenerator: TableGenerator = WGSSummaryGenerator(clinical, molecularHistory, cohorts, keyWidth, valueWidth)
            table.addCell(Cells.createSubTitle(wgsGenerator.title()))
            table.addCell(Cells.create(wgsGenerator.contents()))
        } else {
            val noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth)
            noRecent.addCell(Cells.createKey(molecular.type.display() + " results"))
            noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"))
            table.addCell(Cells.create(noRecent))
        }
        val priorMolecularResultGenerator = PriorMolecularResultGenerator(molecularHistory, keyWidth, valueWidth)
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.create(priorMolecularResultGenerator.contents()))
        return table
    }

    companion object {
        private val LOGGER = LogManager.getLogger(MolecularSummaryGenerator::class.java)
    }
}