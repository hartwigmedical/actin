package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryCRCGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleDutchExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleExternalTrialGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.treatment.EligibleOtherCountriesExternalTrialsGenerator
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
        val aggregatedEvidence = AggregatedEvidenceFactory.create(report.molecular)
        val externalEligibleTrials = aggregatedEvidence.externalEligibleTrialsPerEvent
        val dutchTrials = EligibleExternalTrialGeneratorFunctions.dutchTrials(externalEligibleTrials)
        val nonDutchTrials = EligibleExternalTrialGeneratorFunctions.nonDutchTrials(externalEligibleTrials)

        val generators = listOfNotNull(
            PatientClinicalHistoryCRCGenerator(report.clinical, report.molecular, keyWidth, valueWidth),
            EligibleApprovedTreatmentGenerator(
                report.clinical,
                report.molecular,
                report.treatmentMatch.standardOfCareMatches?.filter { it.eligible() },
                contentWidth(),
                "CRC"
            ),
            EligibleActinTrialsGenerator.forOpenCohortsWithSlots(cohorts, report.treatmentMatch.trialSource, contentWidth()),
            EligibleActinTrialsGenerator.forOpenCohortsWithNoSlots(cohorts, report.treatmentMatch.trialSource, contentWidth()),
            if (dutchTrials.isNotEmpty()) {
                EligibleDutchExternalTrialsGenerator(report.molecular.externalTrialSource, dutchTrials, contentWidth())
            } else null,
            if (nonDutchTrials.isNotEmpty()) {
                EligibleOtherCountriesExternalTrialsGenerator(report.molecular.externalTrialSource, nonDutchTrials, contentWidth())
            } else null
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