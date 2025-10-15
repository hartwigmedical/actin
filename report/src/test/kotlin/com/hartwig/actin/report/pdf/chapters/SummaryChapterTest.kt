package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.ProxyApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.trial.TrialsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SummaryChapterTest {

    private val proper = TestReportFactory.createProperTestReport()

    @Test
    fun `Should provide expected summary tables for default configuration`() {
        val generators = createTestSummaryChapter(TestReportFactory.createExhaustiveTestReport()).createSummaryGenerators()

        assertThat(generators.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    @Test
    fun `Should match total cohort size between report and input`() {
        val report = TestReportFactory.createExhaustiveTestReport()
        val eligibleTrialGenerators = createTestSummaryChapter(TestReportFactory.createExhaustiveTestReport()).createSummaryGenerators()
            .filterIsInstance<EligibleTrialGenerator>()

        assertThat(eligibleTrialGenerators).hasSize(2)
        assertReportCohortSizeMatchesInput(report, eligibleTrialGenerators)
    }

    @Test
    fun `Should match total cohort size between report and input when there are multiple locations`() {
        val matches = TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches
        val trialMatch1 = matches[0].copy(identification = matches[0].identification.copy(source = TrialSource.LKO))
        val trialMatch2 = matches[1].copy(identification = matches[1].identification.copy(source = TrialSource.NKI))
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch1, trialMatch2))
        )

        val eligibleTrialGenerators = createTestSummaryChapter(report).createSummaryGenerators().filterIsInstance<EligibleTrialGenerator>()

        assertThat(eligibleTrialGenerators).hasSize(2)
        assertReportCohortSizeMatchesInput(report, eligibleTrialGenerators)
    }

    @Test
    fun `Should show eligible approved treatments options for CUP with high prediction`() {
        val cupTumor = TumorDetails(name = "Some ${TumorDetailsInterpreter.CUP_STRING}")
        val molecularTests =
            listOf(
                TestMolecularFactory.createMinimalWholeGenomeTest().copy(
                    characteristics = TestMolecularFactory.createMinimalTestCharacteristics()
                        .copy(predictedTumorOrigin = TestMolecularFactory.createHighConfidenceCupPrediction())
                )
            )
        val report = TestReportFactory.createExhaustiveTestReport()
        val generators = createTestSummaryChapter(
            report.copy(patientRecord = report.patientRecord.copy(tumor = cupTumor, molecularTests = molecularTests))
        ).createSummaryGenerators()

        assertThat(generators.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            MolecularSummaryGenerator::class,
            ProxyApprovedTreatmentGenerator::class,
            EligibleTrialGenerator::class,
        )
    }

    @Test
    fun `Should omit external trial generators from summary when molecular results not available`() {
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            patientRecord = proper.patientRecord.copy(molecularTests = emptyList())
        )
        val generators = createTestSummaryChapter(report).createSummaryGenerators()

        assertThat(generators.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    private fun assertReportCohortSizeMatchesInput(report: Report, eligibleTrialGenerators: List<EligibleTrialGenerator>) {
        val trialMatchingOtherResultsChapter =
            ReportContentProvider(report).provideChapters().filterIsInstance<TrialMatchingOtherResultsChapter>().first()
        val generators = trialMatchingOtherResultsChapter.createTrialTableGenerators()
        val trialTableGenerators = generators.filterIsInstance<TrialTableGenerator>()

        val totalCohortSizeOnReport =
            eligibleTrialGenerators.sumOf { it.cohortSize() } + trialTableGenerators.sumOf { it.cohortSize() }
        val totalCohortSizeInput = report.treatmentMatch.trialMatches.sumOf { (it.cohorts.size + it.nonEvaluableCohorts.size) }

        assertThat(totalCohortSizeInput).isEqualTo(totalCohortSizeOnReport)
    }

    private fun createTestSummaryChapter(report: Report): SummaryChapter {
        return SummaryChapter(report, createTrialsProvider(report))
    }

    private fun createTrialsProvider(report: Report): TrialsProvider {
        return TrialsProvider.create(
            report.patientRecord,
            report.treatmentMatch,
            report.configuration.countryOfReference,
            report.configuration.filterOnSOCExhaustionAndTumorType
        )
    }
}