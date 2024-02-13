package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.report.pdf.tables.treatment.EfficacyEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class EfficacyEvidenceChapter() : ReportChapter {
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
        val efficacyEvidenceGenerator = EfficacyEvidenceGenerator(
            listOf(
                OtherTreatment(name = "FOLFOXIRI + bevacizumab", isSystemic = false, categories = emptySet()),
                OtherTreatment(name = "FOLFIRI + bevacizumab", isSystemic = false, categories = emptySet())
            ), contentWidth()
        )
        table.addCell(Cells.createTitle(efficacyEvidenceGenerator.title()))
        table.addCell(Cells.createKey("As first line treatment, the following standard of care treatment(s) could be an option for this patient. Options are ranked by PFS. For further details per study see 'SOC literature details' section in extended report."))
        table.addCell(Cells.create(efficacyEvidenceGenerator.contents()))
        document.add(table)
    }
}