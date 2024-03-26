package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    private val recommendationEngine = mockk<RecommendationEngine>()
    private val recommendationEngineFactory = mockk<RecommendationEngineFactory> { every { create(any()) } returns recommendationEngine }
    private val function = HasExhaustedSOCTreatments(recommendationEngineFactory)

    @Test
    fun `Should return undetermined for empty treatment list when SOC cannot be evaluated`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should return not evaluated for non empty treatment list when SOC cannot be evaluated`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when patient is known to have exhausted SOC`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { recommendationEngine.patientHasExhaustedStandardOfCare(any()) } returns true
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient is known to have not exhausted SOC`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { recommendationEngine.patientHasExhaustedStandardOfCare(any()) } returns false
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }
}