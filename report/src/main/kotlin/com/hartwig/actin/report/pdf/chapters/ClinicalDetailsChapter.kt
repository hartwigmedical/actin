package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class ClinicalDetailsChapter(private val report: Report, override val include: Boolean) : ReportChapter {

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
        val valueWidth = contentWidth - (keyWidth + Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE)

        val generators = ReportContentProvider(report).provideClinicalDetailsTables(keyWidth, valueWidth, contentWidth)
        TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }
}