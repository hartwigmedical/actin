package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.EfficacyEvidenceDetailsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class EfficacyEvidenceDetailsChapter(private val report: Report, override val include: Boolean) : ReportChapter {
    override fun name(): String {
        return "SOC literature details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addEfficacyEvidenceDetails(document)
    }

    private fun addEfficacyEvidenceDetails(document: Document) {
        val socMatches = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
        val table = Tables.createSingleColWithWidth(contentWidth())

        val allAnnotations = socMatches?.flatMap { it.annotations } ?: emptyList()
        if (allAnnotations.isNotEmpty()) {
            allAnnotations.distinctBy { it.acronym }
                .flatMap { annotation ->
                    val generator = EfficacyEvidenceDetailsGenerator(annotation, contentWidth())
                    sequenceOf(Cells.createSubTitle(generator.title()), Cells.create(generator.contents()), Cells.createEmpty())
                }
                .dropLast(1)
                .forEach(table::addCell)
            
            document.add(table)
        } else {
            document.add(Paragraph("There are no standard of care treatment options for this patient").addStyle(Styles.tableContentStyle()))
        }
    }
}