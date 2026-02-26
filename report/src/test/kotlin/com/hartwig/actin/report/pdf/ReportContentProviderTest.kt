package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReportContentProviderTest {

    private val proper = TestReportFactory.createProperTestReport()
    private val configuration = ReportConfiguration()
    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()

    @Test
    fun `Should include molecular chapter and omit efficacy chapters by default`() {
        val chapters = ReportContentProvider(proper, configuration, doidModel).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            MolecularDetailsChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingDetailsChapter::class,
        )
    }

    @Test
    fun `Should match total cohort size between summary and trial matching details`() {
        val report = TestReportFactory.createExhaustiveTestReport()
        val summaryChapter =
            ReportContentProvider(report, configuration, doidModel).provideChapters().filterIsInstance<SummaryChapter>().first()
        val eligibleTrialGenerators = summaryChapter.createSummaryGenerators().filterIsInstance<EligibleTrialGenerator>()

        assertThat(eligibleTrialGenerators).hasSize(4)
        assertReportCohortSizeMatchesInput(report, eligibleTrialGenerators)
    }

    @Test
    fun `Should match total cohort size between summary and trial matching details when there are multiple locations`() {
        val matches = TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches
        val trialMatch1 = matches[0].copy(identification = matches[0].identification.copy(source = TrialSource.LKO))
        val trialMatch2 = matches[1].copy(identification = matches[1].identification.copy(source = TrialSource.NKI))
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch1, trialMatch2))
        )

        val summaryChapter =
            ReportContentProvider(report, configuration, doidModel).provideChapters().filterIsInstance<SummaryChapter>().first()
        val eligibleTrialGenerators = summaryChapter.createSummaryGenerators().filterIsInstance<EligibleTrialGenerator>()

        assertThat(eligibleTrialGenerators).hasSize(3)
        assertReportCohortSizeMatchesInput(report, eligibleTrialGenerators)
    }

    private fun assertReportCohortSizeMatchesInput(report: Report, eligibleTrialGenerators: List<EligibleTrialGenerator>) {
        val trialMatchingDetailsChapter = chapters(report).filterIsInstance<TrialMatchingDetailsChapter>().first()
        val generators = trialMatchingDetailsChapter.createTrialTableGenerators()
        val trialTableGenerators = generators.filterIsInstance<TrialTableGenerator>()

        val totalCohortSizeOnReport = eligibleTrialGenerators.sumOf { it.cohortSize() } + trialTableGenerators.sumOf { it.cohortSize() }
        val totalCohortSizeInput = report.treatmentMatch.trialMatches.sumOf { (it.cohorts.size + it.nonEvaluableCohorts.size) }

        assertThat(totalCohortSizeInput).isEqualTo(totalCohortSizeOnReport)
    }

    private fun chapters(report: Report): List<ReportChapter> {
        return ReportContentProvider(report, configuration, doidModel).provideChapters()
    }
}