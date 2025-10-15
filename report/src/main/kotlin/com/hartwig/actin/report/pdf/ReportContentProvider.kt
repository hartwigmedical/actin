package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.trial.TrialsProvider
import com.hartwig.actin.treatment.EvidenceScoringModel
import com.hartwig.actin.treatment.TreatmentRankingModel
import com.hartwig.actin.treatment.createScoringConfig

class ReportContentProvider(private val report: Report) {

    private val trialsProvider = TrialsProvider.create(
        patientRecord = report.patientRecord,
        treatmentMatch = report.treatmentMatch,
        countryOfReference = report.configuration.countryOfReference,
        retainOriginalExternalTrials = report.configuration.trialMatchingChapterType == TrialMatchingChapterType.COMPREHENSIVE,
        filterOnSOCExhaustionAndTumorType = report.configuration.filterOnSOCExhaustionAndTumorType
    )
    private val treatmentRankingModel = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig()))

    fun provideChapters(): List<ReportChapter> {
        return listOf(
            SummaryChapter(report, trialsProvider),
            MolecularDetailsChapter(report, trialsProvider),
            EfficacyEvidenceChapter(report, treatmentRankingModel),
            ClinicalDetailsChapter(report),
            TrialMatchingDetailsChapter(report, trialsProvider)
        ).filter(ReportChapter::include)
    }
}