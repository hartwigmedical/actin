package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.trial.TrialsProvider

class ReportContentProvider(private val report: Report, private val configuration: ReportConfiguration) {

    private val trialsProvider = TrialsProvider.create(
        patientRecord = report.patientRecord,
        treatmentMatch = report.treatmentMatch,
        countryOfReference = configuration.countryOfReference,
        retainOriginalExternalTrials = configuration.trialMatchingChapterType == TrialMatchingChapterType.COMPLETE,
        filterOnSOCExhaustionAndTumorType = configuration.filterOnSOCExhaustionAndTumorType
    )
    
    fun provideChapters(): List<ReportChapter> {
        return listOf(
            SummaryChapter(report, configuration, trialsProvider),
            MolecularDetailsChapter(report, configuration, trialsProvider),
            EfficacyEvidenceChapter(report, configuration),
            ClinicalDetailsChapter(report, configuration),
            TrialMatchingDetailsChapter(report, configuration, trialsProvider)
        ).filter(ReportChapter::include)
    }
}