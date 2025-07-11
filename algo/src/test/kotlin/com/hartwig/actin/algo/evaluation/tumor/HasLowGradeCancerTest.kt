package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasLowGradeCancerTest {
    private val function = HasLowGradeCancer()

    @Test
    fun `Should fail when high grade cancer`() {
        val wrongCancerType = TumorTestFactory.withDoidAndName("", "high-grade")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(wrongCancerType))
    }

    @Test
    fun `Should pass when low grade cancer`() {
        val wrongCancerType = TumorTestFactory.withDoidAndName("", "low-grade")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(wrongCancerType))
    }

    @Test
    fun `Should resolve to undetermined if terms not found`() {
        val wrongCancerType = TumorTestFactory.withDoidAndName("", "other term")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(wrongCancerType))
    }
}