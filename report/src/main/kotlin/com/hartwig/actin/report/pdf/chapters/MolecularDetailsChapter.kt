package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.interpretation.PriorIHCTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.MolecularCharacteristicsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularDriversGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PredictedTumorOriginGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PriorIHCResultGenerator
import com.hartwig.actin.report.pdf.tables.molecular.WGSSummaryGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Div

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
        if (includeRawPathologyReport) report.patientRecord.tumor.rawPathologyReport?.let { addPathologyReport(document) }
    }

    private fun addMolecularDetails(document: Document) {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val priorIHCResultGenerator =
            PriorIHCResultGenerator(report.patientRecord, keyWidth, contentWidth() - keyWidth - 10, PriorIHCTestInterpreter())
        val priorIHCResults = priorIHCResultGenerator.contents().setBorder(Border.NO_BORDER)
        document.add(priorIHCResults)

        val cohorts =
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)

        val orangeMolecularTable = Tables.createSingleColWithWidth(contentWidth()).addCell(Cells.createEmpty())
        report.patientRecord.molecularHistory.latestOrangeMolecularRecord()?.let { molecular ->
            orangeMolecularTable.addCell(
                Cells.createTitle("${molecular.experimentType.display()} (${molecular.sampleId}, ${date(molecular.date)})")
            )
            if (molecular.hasSufficientQualityButLowPurity()) {
                val purityString = molecular.characteristics.purity?.let { Formats.percentage(it) } ?: "NA"
                val cuppaModeIsWGTS = if(molecular.characteristics.predictedTumorOrigin?.cuppaMode() == CuppaMode.WGTS) " (WGTS)" else ""
                orangeMolecularTable.addCell(
                    Cells.createContentNoBorder(
                        ("Low tumor purity (${purityString}) indicating that potential (subclonal) " +
                                "DNA aberrations might not have been detected & predicted tumor origin${cuppaModeIsWGTS} results may be less reliable")
                    )
                )

            }

            val generators = listOf(MolecularCharacteristicsGenerator(molecular)) + tumorDetailsGenerators(molecular, cohorts, trials)
            TableGeneratorFunctions.addGenerators(generators, orangeMolecularTable, overrideTitleFormatToSubtitle = true)

            if (!molecular.hasSufficientQuality) {
                orangeMolecularTable.addCell(
                    Cells.createContent(
                        ("No successful OncoAct WGS and/or tumor NGS panel could be "
                                + "performed on the submitted biopsy (insufficient quality for reporting)")
                    )
                )
            }
        } ?: orangeMolecularTable.addCell(Cells.createContent("No OncoAct WGS and/or Hartwig NGS panel performed"))
        document.add(orangeMolecularTable)

        val externalPanelResults = report.patientRecord.molecularHistory.molecularTests.filter { it.experimentType == ExperimentType.PANEL }
        for (panel in externalPanelResults) {
            WGSSummaryGenerator(
                true,
                report.patientRecord,
                panel,
                cohorts,
                keyWidth,
                contentWidth() - keyWidth
            ).apply {
                val table = Tables.createSingleColWithWidth(contentWidth())
                table.addCell(Cells.createTitle(title()))
                table.addCell(Cells.create(contents()))
                document.add(table)
            }
        }
    }

    private fun tumorDetailsGenerators(
        molecular: MolecularRecord,
        evaluated: List<InterpretedCohort>,
        trials: Set<ExternalTrialSummary>
    ): List<TableGenerator> {
        return if (molecular.hasSufficientQuality) {
            listOf(
                PredictedTumorOriginGenerator(molecular),
                MolecularDriversGenerator(molecular, evaluated, trials)
            )
        } else emptyList()
    }

    private fun addPathologyReport(document: Document) {
        document.add(Div().setHeight(20F))
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = PathologyReportGenerator(report.patientRecord.tumor)
        // KD: This table doesn't fit in the typical generator format since it contains one row but with a lot of lines. 
        table.addCell(Cells.createTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }
}