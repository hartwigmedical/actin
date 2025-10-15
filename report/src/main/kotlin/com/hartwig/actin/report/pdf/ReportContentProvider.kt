package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import com.hartwig.actin.report.trial.TrialsProvider
import com.hartwig.actin.treatment.EvidenceScoringModel
import com.hartwig.actin.treatment.TreatmentRankingModel
import com.hartwig.actin.treatment.createScoringConfig

class ReportContentProvider(private val report: Report, private val enableExtendedMode: Boolean = false) {

    private val trialsProvider = TrialsProvider.create(
        report.patientRecord,
        report.treatmentMatch,
        report.configuration.countryOfReference,
        report.configuration.filterOnSOCExhaustionAndTumorType,
        enableExtendedMode
    )
    private val treatmentRankingModel = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig()))

    fun provideChapters(): List<ReportChapter> {
        return listOf(
            SummaryChapter(report, trialsProvider),
            MolecularDetailsChapter(report, trialsProvider.evaluableCohortsAndNotIgnore(), trialsProvider.externalTrials().allFiltered()),
            EfficacyEvidenceChapter(report, treatmentRankingModel),
            ClinicalDetailsChapter(report),
            TrialMatchingOtherResultsChapter(
                report,
                externalTrialsOnly = report.configuration.includeOnlyExternalTrialsInTrialMatching,
                trialsProvider,
                include = report.configuration.includeTrialMatchingChapter
            ),
            TrialMatchingDetailsChapter(report, include = enableExtendedMode)
        ).filter(ReportChapter::include)
    }
}