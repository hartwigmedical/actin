package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.ResistanceEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class ResistanceEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {
    override fun name(): String {
        return "Resistance evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        val eligibleSocTreatments = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
            ?.toSet() ?: emptySet()

        addChapterTitle(document)

        val table = Tables.createSingleColWithWidth(contentWidth())
        val resistanceEvidenceGenerator = ResistanceEvidenceGenerator(eligibleSocTreatments, contentWidth())
        table.addCell(Cells.createTitle(resistanceEvidenceGenerator.title()))
        table.addCell(Cells.create(resistanceEvidenceGenerator.contents()))
        document.add(table)
    }
}