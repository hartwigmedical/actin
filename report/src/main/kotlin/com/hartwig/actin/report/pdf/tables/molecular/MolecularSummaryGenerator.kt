package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.report.interpretation.IHCTestInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.LogManager

class MolecularSummaryGenerator(
    private val patientRecord: PatientRecord,
    private val cohorts: List<InterpretedCohort>,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val isShort: Boolean,
    private val molecularTestFilter: MolecularTestFilter
) : TableGenerator {

    private val logger = LogManager.getLogger(MolecularSummaryGenerator::class.java)

    override fun title(): String {
        return "Recent molecular results"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()

        patientRecord.pathologyReports
            ?.takeIf { reports -> reports.any { !it.tissueId.isNullOrBlank() } }
            ?.let {
                val generator = PathologyReportsOverviewGenerator(patientRecord.pathologyReports)
                table.addCell(Cells.createSubTitle(generator.title()))
                table.addCell(Cells.create(generator.contents()))
                table.addCell(Cells.createEmpty())
            }

        val nonIhcTestsIncludedInTrialMatching =
            molecularTestFilter.apply(patientRecord.molecularHistory.molecularTests).filterNot { it.experimentType == ExperimentType.IHC }
        for (molecularTest in nonIhcTestsIncludedInTrialMatching.sortedByDescending { it.date }) {
            if ((molecularTest as? MolecularRecord)?.hasSufficientQuality != false) {
                if (molecularTest.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
                    logger.warn("Generating WGS results for non-WGS sample")
                }
                val wgsGenerator = WGSSummaryGenerator(
                    isShort,
                    patientRecord,
                    molecularTest,
                    cohorts,
                    keyWidth,
                    valueWidth
                )
                table.addCell(Cells.createSubTitle(wgsGenerator.title()))
                table.addCell(Cells.create(wgsGenerator.contents()))
            } else {
                val noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth)
                noRecent.addCell(Cells.createKey(molecularTest.experimentType.display() + " results"))
                noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"))
                table.addCell(Cells.create(noRecent))
            }
        }

        val molecularResultGenerator = IHCResultGenerator(patientRecord, keyWidth, valueWidth, IHCTestInterpreter())
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.create(molecularResultGenerator.contents()))
        return table
    }
}