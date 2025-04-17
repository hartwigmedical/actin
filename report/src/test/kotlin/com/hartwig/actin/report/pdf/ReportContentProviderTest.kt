package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.PersonalizedEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.ResistanceEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryWithOverviewGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCEligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import junit.framework.TestCase.assertEquals
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
            TrialMatchingChapter::class,
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
            TrialMatchingChapter::class,
            TrialMatchingDetailsChapter::class
        )
    }

    @Test
    fun `Should omit molecular chapter and include efficacy chapter and resistance evidence chapter when CRC profile is provided`() {
        val report = proper.copy(
            config = EnvironmentConfiguration.create(null, "CRC").report
        )
        val enableExtendedMode = false
        val chapters = ReportContentProvider(report, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            PersonalizedEvidenceChapter::class,
            ResistanceEvidenceChapter::class,
            EfficacyEvidenceChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingChapter::class
        )
    }

    @Test
    fun `Should omit molecular chapter and include both efficacy chapters and resistance evidence chapter when CRC profile is provided in extended mode`() {
        val report = proper.copy(
            config = EnvironmentConfiguration.create(null, "CRC").report
        )
        val enableExtendedMode = true
        val chapters = ReportContentProvider(report, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            PersonalizedEvidenceChapter::class,
            ResistanceEvidenceChapter::class,
            EfficacyEvidenceChapter::class,
            ClinicalDetailsChapter::class,
            EfficacyEvidenceDetailsChapter::class,
            TrialMatchingChapter::class
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
            EligibleApprovedTreatmentGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class
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
            EligibleApprovedTreatmentGenerator::class,
            EligibleTrialGenerator::class
        )
    }

    @Test
    fun `Should omit molecular table summary and include SOC treatments in summary when using CRC profile`() {
        val report = TestReportFactory.createExhaustiveTestReport().copy(
            config = EnvironmentConfiguration.create(null, "CRC").report
        )
        val tables = ReportContentProvider(report).provideSummaryTables(KEY_WIDTH, VALUE_WIDTH, emptyList())

        assertThat(tables.map { it::class }).containsExactly(
            PatientClinicalHistoryWithOverviewGenerator::class,
            SOCEligibleApprovedTreatmentGenerator::class,
            EligibleTrialGenerator::class,
            EligibleTrialGenerator::class,
            IneligibleTrialGenerator::class
        )
    }

    private fun assertReportCohortSizeMatchesInput(report: Report, eligibleTrialGenerators: List<EligibleTrialGenerator>) {
        val trialMatchingChapter = ReportContentProvider(report).provideChapters().filterIsInstance<TrialMatchingChapter>().first()
        val generators = trialMatchingChapter.createGenerators()
        val trialTableGenerators = generators.filterIsInstance<TrialTableGenerator>()
        
        val totalCohortSizeOnReport =
            eligibleTrialGenerators.sumOf { it.cohortSize() } + trialTableGenerators.sumOf { it.cohortSize() }
        val totalCohortSizeInput = report.treatmentMatch.trialMatches.sumOf { (it.cohorts.size + it.nonEvaluableCohorts.size) }

        assertEquals(totalCohortSizeOnReport, totalCohortSizeInput)
    }
}