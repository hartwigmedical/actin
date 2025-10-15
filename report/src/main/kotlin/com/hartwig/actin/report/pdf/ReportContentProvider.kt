package com.hartwig.actin.report.pdf

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.trial.TrialsProvider
import com.hartwig.actin.treatment.EvidenceScoringModel
import com.hartwig.actin.treatment.TreatmentRankingModel
import com.hartwig.actin.treatment.createScoringConfig

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {
    
    private val trialsProvider = TrialsProvider.create(
        report.patientRecord,
        report.treatmentMatch,
        report.configuration.countryOfReference,
        report.configuration.filterOnSOCExhaustionAndTumorType
    )
    private val treatmentRankingModel = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig()))

    fun provideChapters(): List<ReportChapter> {
        val externalTrials = trialsProvider.externalTrials()

        return listOf(
            SummaryChapter(report, trialsProvider),
            MolecularDetailsChapter(report, trialsProvider.evaluableCohortsAndNotIgnore(), externalTrials.allFiltered()),
            EfficacyEvidenceChapter(report, treatmentRankingModel),
            ClinicalDetailsChapter(report, include = report.configuration.includeClinicalDetailsChapter),
            TrialMatchingOtherResultsChapter(
                report,
                externalTrialsOnly = report.configuration.includeOnlyExternalTrialsInTrialMatching,
                trialsProvider,
                include = report.configuration.includeTrialMatchingChapter
            ),
            TrialMatchingDetailsChapter(report, include = enableExtendedMode)
        ).filter(ReportChapter::include)
    }

    fun provideClinicalDetailsTables(keyWidth: Float, valueWidth: Float): List<TableGenerator> {
        return listOfNotNull(
            ClinicalSummaryGenerator(report = report, showDetails = true, keyWidth = keyWidth, valueWidth = valueWidth),
            PatientCurrentDetailsGenerator(
                record = report.patientRecord,
                keyWidth = keyWidth,
                valueWidth = valueWidth,
                referenceDate = report.treatmentMatch.referenceDate
            ),
            TumorDetailsGenerator(record = report.patientRecord, keyWidth = keyWidth, valueWidth = valueWidth),
            report.patientRecord.medications?.let {
                MedicationGenerator(
                    medications = it,
                    interpreter = MedicationStatusInterpreterOnEvaluationDate(report.treatmentMatch.referenceDate, null)
                )
            },
            if (report.patientRecord.bloodTransfusions.isEmpty()) null else
                BloodTransfusionGenerator(bloodTransfusions = report.patientRecord.bloodTransfusions)
        )
    }
}