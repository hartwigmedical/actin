package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.EfficacyEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class EfficacyEvidenceChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "SOC literature efficacy evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addEfficacyEvidenceDetails(document)
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addEfficacyEvidenceDetails(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val efficacyEvidenceGenerator = EfficacyEvidenceGenerator(report.treatmentMatch.standardOfCareMatches?.filter { it.eligible() }, contentWidth())
        table.addCell(Cells.createTitle(efficacyEvidenceGenerator.title()))
        table.addCell(Cells.createKey("The following standard of care treatment(s) could be an option for this patient. For further details per study see 'SOC literature details' section in extended report."))
        table.addCell(Cells.create(efficacyEvidenceGenerator.contents()))
        document.add(table)
    }
}