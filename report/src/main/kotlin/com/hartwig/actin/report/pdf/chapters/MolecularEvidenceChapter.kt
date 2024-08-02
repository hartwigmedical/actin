package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.molecular.ExternalMolecularTestFreeTextGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(
    private val report: Report, override val include: Boolean, private val includeExternalMolecularTestFreeText: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Molecular Evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularEvidenceTable(document)
        if (includeExternalMolecularTestFreeText) addExternalMolecularTestText(document)
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularClinicalEvidenceGenerator(report.patientRecord.molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }

    private fun addExternalMolecularTestText(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = ExternalMolecularTestFreeTextGenerator(report.patientRecord.molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }
}