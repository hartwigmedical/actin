package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.ProxyStandardOfCareGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.trial.TrialsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SummaryChapterTest {

    @Test
    fun `Should provide expected summary tables for default configuration`() {
        val generators = createTestSummaryChapter(TestReportFactory.createExhaustiveTestReport()).createSummaryGenerators()

        assertThat(generators.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
        )
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
            ProxyStandardOfCareGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    @Test
    fun `Should omit external trial generators from summary when molecular results not available`() {
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            patientRecord = TestReportFactory.createProperTestReport().patientRecord.copy(molecularTests = emptyList())
        )
        val generators = createTestSummaryChapter(report).createSummaryGenerators()

        assertThat(generators.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    private fun createTestSummaryChapter(report: Report): SummaryChapter {
        val configuration = ReportConfiguration()
        return SummaryChapter(report, configuration, createTrialsProvider(report, configuration))
    }

    private fun createTrialsProvider(report: Report, configuration: ReportConfiguration): TrialsProvider {
        return TrialsProvider.create(
            report.patientRecord,
            report.treatmentMatch,
            configuration.countryOfReference,
            configuration.trialMatchingChapterType == TrialMatchingChapterType.DETAILED_ALL_TRIALS,
            configuration.filterOnSOCExhaustionAndTumorType,
        )
    }
}