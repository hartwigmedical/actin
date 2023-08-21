package com.hartwig.actin.report.pdf.chapters

import com.google.common.collect.Lists
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.LabResultsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class ClinicalDetailsChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "Clinical Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addClinicalDetails(document)
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addClinicalDetails(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth - 10
        val generators: MutableList<TableGenerator> = Lists.newArrayList(
            PatientClinicalHistoryGenerator(
                report.clinical(), keyWidth, valueWidth
            ),
            PatientCurrentDetailsGenerator(report.clinical(), keyWidth, valueWidth),
            TumorDetailsGenerator(report.clinical(), keyWidth, valueWidth),
            LabResultsGenerator.Companion.fromRecord(report.clinical(), keyWidth, valueWidth),
            MedicationGenerator(report.clinical().medications(), contentWidth())
        )
        if (!report.clinical().bloodTransfusions().isEmpty()) {
            generators.add(BloodTransfusionGenerator(report.clinical().bloodTransfusions(), contentWidth()))
        }
        for (i in generators.indices) {
            val generator = generators[i]
            table.addCell(Cells.createTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }
}