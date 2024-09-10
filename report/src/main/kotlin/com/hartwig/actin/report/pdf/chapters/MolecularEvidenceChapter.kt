package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.molecular.MolecularEfficacyDescriptionGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    private val molecularHistory = report.patientRecord.molecularHistory

    override fun name(): String {
        return "Molecular Evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularEvidenceTable(document)
        addEfficacyDescriptionTable(document)
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(molecularHistory, contentWidth())
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(onLabelGenerator.title()))
        table.addCell(Cells.create(onLabelGenerator.contents()))
        table.addCell(Cells.createSubTitle(offLabelGenerator.title()))
        table.addCell(Cells.create(offLabelGenerator.contents()))
        document.add(table)
    }

    private fun addEfficacyDescriptionTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularEfficacyDescriptionGenerator(molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }
}