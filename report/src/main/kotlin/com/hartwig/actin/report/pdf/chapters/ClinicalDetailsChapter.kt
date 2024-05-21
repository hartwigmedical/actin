package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class ClinicalDetailsChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "Clinical Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addClinicalDetails(document)
    }

    private fun addClinicalDetails(document: Document) {
        val contentWidth = contentWidth()
        val table = Tables.createSingleColWithWidth(contentWidth)
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth - keyWidth - 10

        val generators = ReportContentProvider(report).provideClinicalDetailsTables(keyWidth, valueWidth, contentWidth)
        
        for (i in generators.indices) {
            val generator = generators[i]
            table.addCell(Cells.createTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }
}