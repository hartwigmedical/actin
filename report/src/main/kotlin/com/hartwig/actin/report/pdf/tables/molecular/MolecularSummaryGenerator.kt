package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.algo.evaluation.molecular.IHCTestFilter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
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
        val nonIhcTestsIncludedInTrialMatching =
            molecularTestFilter.apply(patientRecord.molecularHistory.molecularTests).filterNot { it.experimentType == ExperimentType.IHC }
        val ihcTestsFiltered = IHCTestFilter.mostRecentOrUnknownDateIhcTests(patientRecord.ihcTests).toList()
        val groupedByPathologyReport = PathologyReportFunctions.groupTestsByPathologyReport(
            emptyList(),
            nonIhcTestsIncludedInTrialMatching,
            ihcTestsFiltered,
            patientRecord.pathologyReports
        ).filterValues { (_, molecularTest, ihcTests) -> molecularTest.isNotEmpty() || ihcTests.isNotEmpty() }

        for ((pathologyReport, tests) in groupedByPathologyReport) {
            val (_, molecularTests, ihcTests) = tests
            pathologyReport?.let {
                table.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = pathologyReport)))
                val reportTable = Tables.createSingleCol()
                content(pathologyReport, molecularTests, ihcTests, reportTable)
                table.addCell(Cells.create(reportTable))
            } ?: run {
                if (groupedByPathologyReport.keys.size > 1) {
                    table.addCell(Cells.createTitle("Other Tests"))
                }
                content(pathologyReport, molecularTests, ihcTests, table)
            }
        }

        if (ihcTestsFiltered.isEmpty()) {
            val molecularResultGenerator = IHCResultGenerator(ihcTestsFiltered, keyWidth, valueWidth, IHCTestInterpreter())
            table.addCell(Cells.createSubTitle(molecularResultGenerator.title()))
            table.addCell(Cells.create(molecularResultGenerator.contents()))
        }

        return table
    }

    private fun content(
        pathologyReport: PathologyReport?,
        molecularTests: List<MolecularTest>,
        ihcTests: List<IHCTest>,
        table: Table
    ) {
        for (molecularTest in molecularTests.sortedByDescending { it.date }) {
            if ((molecularTest as? MolecularRecord)?.hasSufficientQuality != false) {
                if (molecularTest.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
                    logger.warn("Generating WGS results for non-WGS sample")
                }
                val wgsGenerator = WGSSummaryGenerator(
                    isShort,
                    patientRecord,
                    molecularTest,
                    pathologyReport,
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

        ihcTests.takeIf { it.isNotEmpty() }?.let {
            val molecularResultGenerator = IHCResultGenerator(ihcTests, keyWidth, valueWidth, IHCTestInterpreter())
            table.addCell(Cells.createSubTitle(molecularResultGenerator.title()))
            table.addCell(Cells.create(molecularResultGenerator.contents()))
        }
    }


}