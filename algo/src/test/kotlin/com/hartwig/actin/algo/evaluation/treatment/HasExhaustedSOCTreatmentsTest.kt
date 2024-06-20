package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    private val recommendationEngine = mockk<RecommendationEngine>()
    private val recommendationEngineFactory = mockk<RecommendationEngineFactory> { every { create() } returns recommendationEngine }
    private val function = HasExhaustedSOCTreatments(recommendationEngineFactory, TestDoidModelFactory.createMinimalTestDoidModel())
    private val nonEmptyTreatmentList = listOf(
        EvaluatedTreatment(
            TreatmentCandidate(
                TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false,
                setOf(EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList()))
            ), listOf(EvaluationFactory.pass("Has MSI"))
        )
    )

    @Test
    fun `Should return undetermined for patient with NSCLC and platinum doublet chemotherapy in treatment history`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val platinumDoublet =
            DrugTreatment(
                name = "Carboplatin+Pemetrexed",
                drugs = setOf(
                    Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
                    Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
                )
            )
        val record = createHistoryWithNSCLCAndTreatment(platinumDoublet)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail for patient with NSCLC with other treatment than platinum doublet chemotherapy in treatment history`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val treatment =
            TreatmentTestFactory.drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
        val record = createHistoryWithNSCLCAndTreatment(treatment)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failGeneralMessages).containsExactly("SOC not exhausted: at least platinum doublet remaining")
    }

    @Test
    fun `Should fail for patient with NSCLC with empty treatment history`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val record = createHistoryWithNSCLCAndTreatment(null)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failGeneralMessages).containsExactly("SOC not exhausted: at least platinum doublet remaining")
    }

    @Test
    fun `Should return undetermined for empty treatment list when SOC cannot be evaluated`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.determineRequiredTreatments(any()) } returns emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should return not evaluated for non empty treatment list when SOC cannot be evaluated`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.determineRequiredTreatments(any()) } returns nonEmptyTreatmentList
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when patient is known to have exhausted SOC`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { recommendationEngine.patientHasExhaustedStandardOfCare(any()) } returns true
        every { recommendationEngine.determineRequiredTreatments(any()) } returns emptyList()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient is known to have not exhausted SOC`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { recommendationEngine.patientHasExhaustedStandardOfCare(any()) } returns false
        every { recommendationEngine.determineRequiredTreatments(any()) } returns nonEmptyTreatmentList
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        assertThat(function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())).failGeneralMessages)
            .containsExactly("Patient has not exhausted SOC (remaining options: pembrolizumab)")
    }

    private fun createHistoryWithNSCLCAndTreatment(drugTreatment: Treatment?): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
            oncologicalHistory = if (drugTreatment != null) {
                listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(listOf(drugTreatment))
                )
            } else emptyList()
        )
    }
}