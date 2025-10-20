package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.configuration.MolecularChapterType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.IhcResultGenerator
import com.hartwig.actin.report.pdf.tables.molecular.LongitudinalMolecularHistoryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OrangeMolecularRecordGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportFunctions
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportGenerator
import com.hartwig.actin.report.pdf.tables.molecular.WgsSummaryGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Table

class MolecularDetailsChapter(
    private val report: Report,
    private val configuration: ReportConfiguration,
    private val trialsProvider: TrialsProvider
) : ReportChapter {

    private val externalTrials = trialsProvider.externalTrials().allFiltered()

    override fun name(): String {
        return "Molecular Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun include(): Boolean {
        return true
    }

    override fun render(document: Document) {
        addChapterTitle(document)

        if (configuration.molecularChapterType == MolecularChapterType.STANDARD ||
            configuration.molecularChapterType == MolecularChapterType.STANDARD_WITH_PATHOLOGY ||
            configuration.molecularChapterType == MolecularChapterType.STANDARD_AND_LONGITUDINAL
        ) {
            addMolecularDetails(document)
            if (configuration.molecularChapterType == MolecularChapterType.STANDARD_WITH_PATHOLOGY ||
                configuration.molecularChapterType == MolecularChapterType.STANDARD_AND_LONGITUDINAL
            ) {
                addPathologyReport(document)
            }
        }

        if (configuration.molecularChapterType == MolecularChapterType.LONGITUDINAL ||
            configuration.molecularChapterType == MolecularChapterType.STANDARD_AND_LONGITUDINAL
        ) {
            addLongitudinalMolecularHistoryTable(document)
        }
    }

    private fun addMolecularDetails(document: Document) {
        val cohorts =
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, configuration.filterOnSOCExhaustionAndTumorType)

        val orangeMolecularTest = MolecularHistory(report.patientRecord.molecularTests).latestOrangeMolecularRecord()
        val externalPanelResults = report.patientRecord.molecularTests.filter { it.experimentType == ExperimentType.PANEL }
        val filteredIhcTests = IhcTestFilter.mostRecentAndUnknownDateIhcTests(report.patientRecord.ihcTests).toList()
        val groupedByPathologyReport = PathologyReportFunctions.groupTestsByPathologyReport(
            listOfNotNull(orangeMolecularTest),
            externalPanelResults,
            filteredIhcTests,
            report.patientRecord.pathologyReports
        )

        val table = Tables.createSingleColWithWidth(contentWidth())
        for ((pathologyReport, tests) in groupedByPathologyReport) {
            pathologyReport ?: groupedByPathologyReport.keys.takeIf { it.size > 1 }?.let {
                table.addCell(Cells.createTitle("Other Tests"))
            }
            val (orangeMolecularRecords, molecularTests, ihcTests) = tests
            contentPerPathologyReport(pathologyReport, orangeMolecularRecords, molecularTests, ihcTests, cohorts, table)
        }
        document.add(table)
    }

    private fun contentPerPathologyReport(
        pathologyReport: PathologyReport?,
        orangeMolecularRecord: List<MolecularTest>,
        externalPanelResults: List<MolecularTest>,
        ihcTests: List<IhcTest>,
        cohorts: List<InterpretedCohort>,
        topTable: Table
    ) {
        pathologyReport?.let {
            topTable.addCell(Cells.create(PathologyReportFunctions.createPathologyReportSummaryCell(pathologyReport = it)))
        }

        val tableWidth = topTable.width.value - 2 * Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = tableWidth - keyWidth

        val orangeGenerators = orangeMolecularRecord.map {
            OrangeMolecularRecordGenerator(externalTrials, cohorts, tableWidth, it, pathologyReport)
        }
        val wgsSummaryGenerators = externalPanelResults.map {
            WgsSummaryGenerator(
                SummaryType.DETAILS,
                report.patientRecord,
                it,
                pathologyReport,
                cohorts,
                keyWidth,
                valueWidth
            )
        }

        val ihcGenerator = if (ihcTests.isNotEmpty()) {
            IhcResultGenerator(ihcTests, keyWidth, valueWidth - 10, IhcTestInterpreter())
        } else null

        TableGeneratorFunctions.addGenerators(
            orangeGenerators + wgsSummaryGenerators + listOfNotNull(ihcGenerator),
            topTable,
            overrideTitleFormatToSubtitle = (pathologyReport != null)
        )
    }

    private fun addPathologyReport(document: Document) {
        report.patientRecord.pathologyReports
            ?.takeIf { reports -> reports.any { it.report.isNotBlank() } }
            ?.let {
                document.add(Div().setHeight(20F))
                val table = Tables.createSingleColWithWidth(contentWidth())
                val generator = PathologyReportGenerator(report.patientRecord.pathologyReports)
                // KD: This table doesn't fit in the typical generator format since it contains one row but with a lot of lines.
                table.addCell(Cells.createTitle(generator.title()))
                table.addCell(Cells.create(generator.contents()))
                document.add(table)
            }
    }

    private fun addLongitudinalMolecularHistoryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val cohorts = trialsProvider.evaluableCohortsAndNotIgnore()
        val generator = LongitudinalMolecularHistoryGenerator(report.patientRecord.molecularTests, cohorts)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }
}