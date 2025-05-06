package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.evaluation.molecular.IHCTestFilter
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.IHCTestInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.IHCResultGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OrangeMolecularRecordGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportFunctions
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportGenerator
import com.hartwig.actin.report.pdf.tables.molecular.WGSSummaryGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Table

class MolecularDetailsChapter(
    private val report: Report,
    override val include: Boolean,
    private val includeRawPathologyReport: Boolean,
    private val trials: Set<ExternalTrialSummary>
) : ReportChapter {

    override fun name(): String {
        return "Molecular Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularDetails(document)
        if (includeRawPathologyReport)
            addRawPathologyReport(document)
    }

    private fun addMolecularDetails(document: Document) {

        val cohorts =
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)

        val orangeMolecularRecord = report.patientRecord.molecularHistory.latestOrangeMolecularRecord()
        val externalPanelResults = report.patientRecord.molecularHistory.molecularTests.filter { it.experimentType == ExperimentType.PANEL }
        val filteredIhcTests = IHCTestFilter.mostRecentOrUnknownDateIhcTests(report.patientRecord.ihcTests).toList()
        val groupedByPathologyReport = PathologyReportFunctions.groupTestsByPathologyReport(
            listOfNotNull(orangeMolecularRecord),
            externalPanelResults,
            filteredIhcTests,
            report.patientRecord.pathologyReports
        ).filterValues { (orangeTests, molecularTest, ihcTests) ->
            orangeTests.isNotEmpty() || molecularTest.isNotEmpty() || ihcTests.isNotEmpty()
        }

        if (orangeMolecularRecord == null) {
            document.add(Cells.createContent("No OncoAct WGS and/or Hartwig NGS panel performed"))
        }

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

    private fun addRawPathologyReport(document: Document) {
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

    private fun contentPerPathologyReport(
        pathologyReport: PathologyReport?,
        orangeMolecularRecord: List<MolecularRecord>,
        externalPanelResults: List<MolecularTest>,
        ihcTests: List<IHCTest>,
        cohorts: List<InterpretedCohort>,
        topTable: Table
    ) {

        pathologyReport?.let {
            topTable.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = it)))
        }

        val tableWidth = topTable.width.value - 2 * Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = tableWidth - keyWidth

        val orangeGenerators = orangeMolecularRecord.map {
            OrangeMolecularRecordGenerator(trials, cohorts, tableWidth, it, pathologyReport)
        }
        val wgsSummaryGenerators = externalPanelResults.map {
            WGSSummaryGenerator(true, report.patientRecord, it, pathologyReport, cohorts, keyWidth, valueWidth)
        }
        val ihcGenerator = ihcTests.takeIf { it.isNotEmpty() }?.let {
            IHCResultGenerator(ihcTests, keyWidth, valueWidth - 10, IHCTestInterpreter())
        }
        TableGeneratorFunctions.addGenerators(
            orangeGenerators + wgsSummaryGenerators + listOfNotNull(ihcGenerator),
            topTable,
            overrideTitleFormatToSubtitle = false
        )
    }
}