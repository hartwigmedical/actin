package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.MolecularEfficacyDescriptionGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(val report: Report, override val include: Boolean) : ReportChapter {

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
        val innerContentWidth = contentWidth() - Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(molecularHistory, innerContentWidth)
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(molecularHistory, innerContentWidth)
        TableGeneratorFunctions.addGenerators(listOf(onLabelGenerator, offLabelGenerator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addEfficacyDescriptionTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val innerContentWidth = contentWidth() - Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val generator = MolecularEfficacyDescriptionGenerator(molecularHistory, innerContentWidth)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }
}