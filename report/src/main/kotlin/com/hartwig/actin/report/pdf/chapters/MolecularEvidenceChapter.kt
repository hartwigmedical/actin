package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.molecular.PathologyReportGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(
    private val report: Report, override val include: Boolean, private val includeRawPathologyReport: Boolean
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
        if (includeRawPathologyReport) report.patientRecord.tumor.rawPathologyReport.let { addPathologyReport(document) }
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularClinicalEvidenceGenerator(report.patientRecord.molecularHistory, contentWidth())
        table.addCell(Cells.createSubTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }

    private fun addPathologyReport(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = PathologyReportGenerator(report.patientRecord.tumor, contentWidth())
        table.addCell(Cells.createSubTitle(generator.title()))
        table.addCell(Cells.create(generator.contents()))
        document.add(table)
    }
}