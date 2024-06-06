package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularCharacteristicsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularDriversGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PredictedTumorOriginGenerator
import com.hartwig.actin.report.pdf.tables.molecular.PriorMolecularResultGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border

class MolecularDetailsChapter(private val report: Report, override val include: Boolean) : ReportChapter {
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

    private fun addMolecularDetails(document: Document) {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val priorMolecularResultGenerator =
            PriorMolecularResultGenerator(
                report.patientRecord.molecularHistory,
                keyWidth,
                contentWidth() - keyWidth - 10,
                PriorMolecularTestInterpreter()
            )
        val priorMolecularResults = priorMolecularResultGenerator.contents().setBorder(Border.NO_BORDER)
        document.add(priorMolecularResults)

        val table = Tables.createSingleColWithWidth(contentWidth())
        table.addCell(Cells.createEmpty())
        report.patientRecord.molecularHistory.latestOrangeMolecularRecord()?.let { molecular ->
            table.addCell(
                Cells.createTitle("${molecular.type.display()} (${molecular.sampleId}, ${date(molecular.date)})")
            )
            val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)
            val generators: MutableList<TableGenerator> = mutableListOf(
                MolecularCharacteristicsGenerator(molecular, contentWidth())
            )
            if (molecular.containsTumorCells) {
                generators.add(PredictedTumorOriginGenerator(molecular, contentWidth()))
                generators.add(MolecularDriversGenerator(report.treatmentMatch.trialSource, molecular, cohorts, report.treatmentMatch.trialMatches, contentWidth()))
            }
            for (i in generators.indices) {
                val generator = generators[i]
                table.addCell(Cells.createSubTitle(generator.title()))
                table.addCell(Cells.create(generator.contents()))
                if (i < generators.size - 1) {
                    table.addCell(Cells.createEmpty())
                }
            }
            if (!molecular.containsTumorCells) {
                table.addCell(Cells.createContent("No successful OncoAct WGS and/or tumor NGS panel could be performed on the submitted biopsy"))
            }
        } ?: table.addCell(Cells.createContent("No OncoAct WGS and/or tumor NGS panel performed"))
        document.add(table)
    }
}