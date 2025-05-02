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

        val orangeMolecularTable = Tables.createSingleColWithWidth(contentWidth()).addCell(Cells.createEmpty())
        orangeMolecularRecord?.also { molecular ->
            val pathologyReport = if (orangePathologyReport.isNotEmpty()) orangePathologyReport.first() else null
            pathologyReport?.let {
                orangeMolecularTable.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = pathologyReport)))
            }
            OrangeMolecularRecordGenerator(report.patientRecord, trials, cohorts, contentWidth(), molecular)
                .apply {
                    val table = Tables.createSingleColWithWidth(contentWidth())
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

        val table = Tables.createSingleColWithWidth(contentWidth()).addCell(Cells.createEmpty())
        for (pathologyReport in otherMolecularReports) {
            groupedByPathologyReport[pathologyReport]
                ?.takeIf { (molecularTest, ihcTests) -> molecularTest.isNotEmpty() || ihcTests.isNotEmpty() }
                ?.let { (molecularTest, ihcTests) ->
                    pathologyReport?.let {
                        table.addCell(Cells.create(PathologyReportFunctions.getPathologyReportSummary(report = pathologyReport)))
                        content(pathologyReport, molecularTest, ihcTests, cohorts, table)
                    } ?: run {
                        if (!report.patientRecord.pathologyReports.isNullOrEmpty()) {
                            table.addCell(Cells.createTitle("Other Tests"))
                        }
                        content(pathologyReport, molecularTest, ihcTests, cohorts, table)
                    }
                }
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
        val keyWidth = Formats.STANDARD_KEY_WIDTH

        for (panel in externalPanelResults) {
            WGSSummaryGenerator(true, report.patientRecord, panel, pathologyReport, cohorts, keyWidth, contentWidth() - keyWidth)
                .apply {
                    val table = Tables.createSingleColWithWidth(contentWidth())
                    table.addCell(Cells.createTitle(title()))
                    table.addCell(Cells.create(contents()))
                    topTable.addCell(Cells.create(table))
                }
        }

        ihcTests.takeIf { it.isNotEmpty() }?.let {
            IHCResultGenerator(ihcTests, keyWidth, contentWidth() - keyWidth - 10, IHCTestInterpreter())
                .apply {
                    val table = Tables.createSingleColWithWidth(contentWidth())
                    table.addCell(Cells.createTitle(title()))
                    table.addCell(Cells.create(contents()))
                    topTable.addCell(Cells.create(table))
                }
        }
    }
}