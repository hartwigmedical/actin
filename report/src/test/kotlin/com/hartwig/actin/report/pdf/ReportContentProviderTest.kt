package com.hartwig.actin.report.pdf

import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f

class ReportContentProviderTest {

    private val proper = TestReportFactory.createProperTestReport()

    @Test
    fun `Should match total cohort size between report and input`() {
        val report = TestReportFactory.createExhaustiveTestReport()
        val eligibleTrialGenerators = ReportContentProvider(report).provideSummaryTables(
            KEY_WIDTH,
            VALUE_WIDTH,
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)
        ).filterIsInstance<EligibleTrialGenerator>()

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
        val eligibleTrialGenerators = ReportContentProvider(report).provideSummaryTables(
            KEY_WIDTH,
            VALUE_WIDTH,
            InterpretedCohortFactory.createEvaluableCohorts(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)
        ).filterIsInstance<EligibleTrialGenerator>()

        assertThat(eligibleTrialGenerators).hasSize(2)
        assertReportCohortSizeMatchesInput(report, eligibleTrialGenerators)
    }

    @Test
    fun `Should include molecular chapter and omit efficacy chapters by default`() {
        val enableExtendedMode = false
        val chapters = ReportContentProvider(proper, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            MolecularDetailsChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingOtherResultsChapter::class,
        )
    }

    @Test
    fun `Should include trial matching details by default in extended mode`() {
        val enableExtendedMode = true
        val chapters = ReportContentProvider(proper, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            MolecularDetailsChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingOtherResultsChapter::class,
            TrialMatchingDetailsChapter::class
        )
    }

    @Test
    fun `Should provide all clinical details tables when details are provided`() {
        val tables = ReportContentProvider(proper).provideClinicalDetailsTables(KEY_WIDTH, VALUE_WIDTH)
        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class,
            MedicationGenerator::class,
            BloodTransfusionGenerator::class
        )
    }

    @Test
    fun `Should omit medication and bloodTransfusion tables when data not provided`() {
        val report = proper.copy(
            patientRecord = proper.patientRecord.copy(
                medications = null,
                bloodTransfusions = emptyList()
            )
        )
        val tables = ReportContentProvider(report).provideClinicalDetailsTables(KEY_WIDTH, VALUE_WIDTH)
        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class
        )
    }

    @Test
    fun `Should provide expected summary tables for default configuration`() {
        val tables = ReportContentProvider(TestReportFactory.createExhaustiveTestReport())
            .provideSummaryTables(KEY_WIDTH, VALUE_WIDTH, emptyList())

        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    @Test
    fun `Should show eligible approved treatments options for CUP with high prediction`() {
        val cupTumor = TumorDetails(name = "Some ${TumorDetailsInterpreter.CUP_STRING}")
        val molecularHistory = MolecularHistory(
            listOf(
                TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                    characteristics = TestMolecularFactory.createMinimalTestCharacteristics()
                        .copy(predictedTumorOrigin = TestMolecularFactory.createHighConfidenceCupPrediction())
                )
            )
        )
        val report = TestReportFactory.createExhaustiveTestReport()
        val tables = ReportContentProvider(
            report.copy(
                patientRecord = report.patientRecord.copy(
                    tumor = cupTumor,
                    molecularHistory = molecularHistory
                )
            )
        )
            .provideSummaryTables(KEY_WIDTH, VALUE_WIDTH, emptyList())

        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryGenerator::class,
            MolecularSummaryGenerator::class,
            EligibleApprovedTreatmentGenerator::class,
            EligibleTrialGenerator::class,
        )
    }

    @Test
    fun `Should omit molecular table and external tables from summary when molecular results not available`() {
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            patientRecord = proper.patientRecord.copy(molecularHistory = MolecularHistory.empty())
        )
        val tables = ReportContentProvider(report).provideSummaryTables(KEY_WIDTH, VALUE_WIDTH, emptyList())

        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryGenerator::class,
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
}