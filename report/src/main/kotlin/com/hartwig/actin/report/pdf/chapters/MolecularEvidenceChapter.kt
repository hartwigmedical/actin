package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.molecular.MolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Paragraph

class MolecularEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    override fun name(): String {
        return "Molecular Evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularEvidenceTable(document)
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(report.patientRecord.molecularHistory, contentWidth())
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(report.patientRecord.molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(onLabelGenerator.title()))
        table.addCell(Cells.create(onLabelGenerator.contents()))
        table.addCell(Cells.createSubTitle(offLabelGenerator.title()))
        table.addCell(Cells.create(offLabelGenerator.contents()))
        document.add(table)
    }
}