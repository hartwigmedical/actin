package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasAnyLesionTest {

    private val function = HasAnyLesion()

    @Test
    fun `Should fail if patient has no lesions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withNoConfirmedLesions()))
    }

    @Test
    fun `Should evaluate to pass if only one type of lesion present`() {
        listOf(
            TumorTestFactory.withBoneLesions(true),
            TumorTestFactory.withLiverLesions(true),
            TumorTestFactory.withCnsLesions(true),
            TumorTestFactory.withBrainLesions(true),
            TumorTestFactory.withLungLesions(true),
            TumorTestFactory.withLymphNodeLesions(true)
        ).forEach { patient -> assertEvaluation(EvaluationResult.PASS, function.evaluate(patient)) }
    }

    @Test
    fun `Should evaluate to pass if only other lesions are present`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(listOf("other"))))
    }

    @Test
    fun `Should evaluate to undetermined if only suspected lesions are present`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }
}