package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryCRCGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryCRCGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class SummaryChapterCRC(private val report: Report) : ReportChapter {

    override fun name(): String {
        return "Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addSummaryTable(document)
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addSummaryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)

        val generators = listOfNotNull(
            PatientClinicalHistoryCRCGenerator(report.clinical, keyWidth, valueWidth),
            MolecularSummaryCRCGenerator(report.molecular, keyWidth, valueWidth),
            EligibleApprovedTreatmentGenerator(
                report.clinical,
                report.molecular,
                report.treatmentMatch.standardOfCareMatches,
                contentWidth(),
                "CRC"
            ),
            EligibleActinTrialsGenerator.forOpenCohortsWithSlots(cohorts, report.treatmentMatch.trialSource, contentWidth()),
            EligibleActinTrialsGenerator.forOpenCohortsWithNoSlots(cohorts, report.treatmentMatch.trialSource, contentWidth()),
        )

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