package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.molecular.LongitudinalMolecularHistoryGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class LongitudinalMolecularHistoryChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    override fun name(): String {
        return "Molecular History"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addLongitudinalMolecularHistoryTable(document)
    }

    private fun addLongitudinalMolecularHistoryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generators =
            listOf(LongitudinalMolecularHistoryGenerator(report.patientRecord.molecularHistory, contentWidth()))

        generators.forEachIndexed { i, generator ->
            // TODO (KD): Make sure subtitle doesn't double-up with title.
            //table.addCell(Cells.createSubTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }
}