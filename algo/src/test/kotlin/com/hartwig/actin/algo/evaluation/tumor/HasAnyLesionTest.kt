package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasAnyLesionTest {

    private val function = HasAnyLesion()

    @Test
    fun `Should fail if patient has no lesions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should evaluate to pass if only one type of lesion present`() {
        listOf(true).forEach { patient ->
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(patient)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(patient)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withCnsLesions(patient)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(patient)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(patient)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLymphNodeLesions(patient)))
        }
    }

    @Test
    fun `Should evaluate to pass if only other lesions are present`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(listOf("other"))))
    }

    @Test
    fun `Should evaluate to undetermined if only suspected lesions are present`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }
}