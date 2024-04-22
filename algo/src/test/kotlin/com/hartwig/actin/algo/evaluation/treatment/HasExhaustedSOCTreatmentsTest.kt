package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    private val recommendationEngine = mockk<RecommendationEngine>()
    private val recommendationEngineFactory = mockk<RecommendationEngineFactory> { every { create() } returns recommendationEngine }
    private val function = HasExhaustedSOCTreatments(recommendationEngineFactory)
    private val nonEmptyTreatmentList = listOf(
        EvaluatedTreatment(
            TreatmentCandidate(
                TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false,
                setOf(EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList()))
            ), listOf(EvaluationFactory.pass("Has MSI"))
        )
    )

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
        Assertions.assertThat(function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())).failGeneralMessages)
            .containsExactly("Patient has not exhausted SOC (remaining options: pembrolizumab)")
    }
}