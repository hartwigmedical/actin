package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.IHCTestInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.IHCResultGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OrangeMolecularRecordGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportFunctions
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportFunctions.date
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
        val filteredIhcTests = IhcTestFilter.mostRecentOrUnknownDateIhcTests(report.patientRecord.ihcTests).toList()
        val groupedByPathologyReport = PathologyReportFunctions.groupTestsByPathologyReport(
            externalPanelResults,
            filteredIhcTests,
            report.patientRecord.pathologyReports
        )

        val (orangePathologyReport, otherMolecularReports) = groupedByPathologyReport.keys.partition { key ->
            key != null && orangeMolecularRecord != null && key.date == orangeMolecularRecord.date
        }

        val orangeMolecularTable = Tables.createSingleColWithWidth(contentWidth())
        val outerTableWidth = orangeMolecularTable.width.value - Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val innerTableWidth = orangeMolecularTable.width.value - 2 * Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE

        orangeMolecularRecord?.also { molecular ->
            val pathologyReport = if (orangePathologyReport.isNotEmpty()) orangePathologyReport.first() else null
            pathologyReport?.let {
                orangeMolecularTable.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = pathologyReport)))
            }
            OrangeMolecularRecordGenerator(report.patientRecord, trials, cohorts, innerTableWidth, molecular)
                .apply {
                    val table = Tables.createSingleColWithWidth(outerTableWidth)
                    table.addCell(Cells.createTitle(title()))
                    table.addCell(Cells.create(contents()))
                    orangeMolecularTable.addCell(Cells.create(table))
                }
            pathologyReport?.let {
                groupedByPathologyReport[pathologyReport]?.let { (molecularTest, ihcTests) ->
                    content(pathologyReport, molecularTest, ihcTests, cohorts, orangeMolecularTable)
                }
            }
        } ?: run { orangeMolecularTable.addCell(Cells.createContent("No OncoAct WGS and/or Hartwig NGS panel performed")) }
        document.add(orangeMolecularTable)

        val filteredGroupedByPathologyReport = groupedByPathologyReport
            .filterKeys { it in otherMolecularReports }
            .filterValues { (molecularTest, ihcTests) -> molecularTest.isNotEmpty() || ihcTests.isNotEmpty() }

        val table = Tables.createSingleColWithWidth(contentWidth())
        for ((pathologyReport, tests) in filteredGroupedByPathologyReport) {
            val (molecularTest, ihcTests) = tests
            pathologyReport?.let {
                table.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = it)))
            }
            filteredGroupedByPathologyReport.keys.takeIf { it.size > 1 }
                ?.let { table.addCell(Cells.createTitle("Other Tests")) }
            content(pathologyReport, molecularTest, ihcTests, cohorts, table)
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


    private fun content(
        pathologyReport: PathologyReport?,
        externalPanelResults: List<MolecularTest>,
        ihcTests: List<IHCTest>,
        cohorts: List<InterpretedCohort>,
        topTable: Table
    ) {
        val innerTableWidth = topTable.width.value - 2 * Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = innerTableWidth - keyWidth

        val wgsSummaryGenerators = externalPanelResults.map { panel ->
            WGSSummaryGenerator(true, report.patientRecord, panel, pathologyReport, cohorts, keyWidth, valueWidth)
        }

        val ihcGenerator = ihcTests.takeIf { it.isNotEmpty() }?.let {
            IHCResultGenerator(ihcTests, keyWidth, valueWidth - 10, IHCTestInterpreter())
        }

        TableGeneratorFunctions.addGenerators(
            wgsSummaryGenerators + listOfNotNull(ihcGenerator),
            topTable,
            overrideTitleFormatToSubtitle = false
        )
    }
}