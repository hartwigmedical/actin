package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularCharacteristicsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularDriversGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PredictedTumorOriginGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PriorMolecularResultGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Paragraph

class MolecularDetailsChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "Molecular Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularDetails(document)
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addMolecularDetails(document: Document) {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val priorMolecularResultGenerator = PriorMolecularResultGenerator(report.clinical, keyWidth, contentWidth() - keyWidth - 10)
        val priorMolecularResults = priorMolecularResultGenerator.contents().setBorder(Border.NO_BORDER)
        document.add(priorMolecularResults)

        val table = Tables.createSingleColWithWidth(contentWidth())
        table.addCell(Cells.createEmpty())
        report.molecular?.let {
            table.addCell(
                Cells.createTitle("${report.molecular.type.display()} (${report.molecular.sampleId}, ${date(report.molecular.date)})")
            )
            val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)
            val generators: MutableList<TableGenerator> = mutableListOf(
                MolecularCharacteristicsGenerator(report.molecular, contentWidth())
            )
            if (report.molecular.containsTumorCells) {
                generators.add(PredictedTumorOriginGenerator(report.molecular, contentWidth()))
                generators.add(MolecularDriversGenerator(report.treatmentMatch.trialSource, report.molecular, cohorts, contentWidth()))
            }
            for (i in generators.indices) {
                val generator = generators[i]
                table.addCell(Cells.createSubTitle(generator.title()))
                table.addCell(Cells.create(generator.contents()))
                if (i < generators.size - 1) {
                    table.addCell(Cells.createEmpty())
                }
            }
            if (!report.molecular.containsTumorCells) {
                table.addCell(Cells.createContent("No successful OncoAct WGS and/or tumor NGS panel could be performed on the submitted biopsy"))
            }
        } ?: table.addCell(Cells.createContent("No OncoAct WGS and/or tumor NGS panel performed"))
        document.add(table)
    }
}