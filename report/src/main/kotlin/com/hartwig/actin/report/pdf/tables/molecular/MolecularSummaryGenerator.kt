package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.LogManager

class MolecularSummaryGenerator(
    private val patientRecord: PatientRecord,
    private val cohorts: List<InterpretedCohort>,
    private val contentWidth: Float,
    private val keyWidth: Float,
    private val valueWidth: Float,
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
        val nonIhcTestsIncludedInTrialMatching = patientRecord.molecularTests.filterNot { it.experimentType == ExperimentType.IHC }
        val trialRelevantEvents = cohorts.flatMap { it.molecularInclusionEvents + it.molecularExclusionEvents }.distinct()
        val ihcTestsFiltered = IhcTestFilter.mostRecentAndUnknownDateIhcTests(patientRecord.ihcTests)
            .filter { ihc -> trialRelevantEvents.any { it.contains(ihc.item, ignoreCase = true) } }
        val testReportsHash = nonIhcTestsIncludedInTrialMatching.mapNotNull { it.reportHash } + ihcTestsFiltered.mapNotNull { it.reportHash }
        val filteredPathologyReports = patientRecord.pathologyReports?.filter { it.reportHash in testReportsHash }
        val groupedByPathologyReport = PathologyReportFunctions.groupTestsByPathologyReport(
            emptyList(),
            nonIhcTestsIncludedInTrialMatching,
            ihcTestsFiltered,
            filteredPathologyReports,
        )

        for ((pathologyReport, tests) in groupedByPathologyReport) {
            val (_, molecularTests, ihcTests) = tests
            pathologyReport?.let {
                table.addCell(Cells.create(PathologyReportFunctions.createPathologyReportSummaryCell(pathologyReport = pathologyReport)))
                val reportTable = Tables.createSingleCol()
                content(pathologyReport, molecularTests, ihcTests, reportTable, valueWidth - Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE)
                table.addCell(Cells.create(reportTable).setPaddingLeft(Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE))
            } ?: run {
                content(pathologyReport, molecularTests, ihcTests, table, valueWidth)
            }
        }

        return table
    }

    private fun content(
        pathologyReport: PathologyReport?,
        molecularTests: List<MolecularTest>,
        ihcTests: List<IhcTest>,
        table: Table,
        valueWidth: Float
    ) {
        for (molecularTest in molecularTests.sortedByDescending { it.date }) {
            val wgsMolecular = MolecularHistory(listOf(molecularTest)).latestOrangeMolecularRecord()
            if (wgsMolecular?.hasSufficientQuality != false) {
                if (molecularTest.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
                    logger.debug("Generating WGS results for non-WGS sample")
                }
                val wgsGenerator = WgsSummaryGenerator(
                    selectSummaryType(molecularTest.experimentType),
                    patientRecord,
                    molecularTest,
                    pathologyReport,
                    cohorts,
                    keyWidth,
                    valueWidth
                )
                if (pathologyReport == null) {
                    table.addCell(Cells.createTitle(wgsGenerator.title()))
                } else {
                    table.addCell(Cells.createSubTitle(wgsGenerator.title()))
                }
                table.addCell(Cells.create(wgsGenerator.contents()))
            } else {
                val noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth)
                noRecent.addCell(Cells.createKey(molecularTest.experimentType.display() + " results"))
                noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"))
                table.addCell(Cells.create(noRecent))
            }
        }

        if (ihcTests.isNotEmpty()) {
            val molecularResultGenerator =
                IhcResultGenerator(ihcTests, keyWidth, valueWidth, IhcTestInterpreter(), "Trial-relevant IHC results")
            table.addCell(Cells.createSubTitle(molecularResultGenerator.title()))
            table.addCell(Cells.create(molecularResultGenerator.contents()))
        }
    }

    private fun selectSummaryType(experimentType: ExperimentType): SummaryType {
        return when {
            experimentType in setOf(ExperimentType.HARTWIG_TARGETED, ExperimentType.PANEL) -> SummaryType.SHORT_SUMMARY
            else -> SummaryType.LONG_SUMMARY
        }
    }
}