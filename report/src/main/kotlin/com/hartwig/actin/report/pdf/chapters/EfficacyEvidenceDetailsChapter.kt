package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EfficacyEvidenceDetailsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class EfficacyEvidenceDetailsChapter(private val socMatches: List<AnnotatedTreatmentMatch>?) : ReportChapter {
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

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addEfficacyEvidenceDetails(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generators: MutableList<TableGenerator> = mutableListOf()
        val allAnnotations = socMatches?.flatMap { it.annotations }?.distinct() ?: emptyList()
        if (allAnnotations.isNotEmpty()) {
            for (annotation in allAnnotations) {
                val efficacyEvidenceGenerator = EfficacyEvidenceDetailsGenerator(annotation, contentWidth())
                generators.add(efficacyEvidenceGenerator)
            }

            for (i in generators.indices) {
                val generator = generators[i]
                table.addCell(Cells.createSubTitle(generator.title()))
                table.addCell(Cells.create(generator.contents()))
                if (i < generators.size - 1) {
                    table.addCell(Cells.createEmpty())
                }
            }
            document.add(table)
        } else {
            document.add(Paragraph("There are no standard of care treatment options for this patient").addStyle(Styles.tableContentStyle()))
        }
    }
}