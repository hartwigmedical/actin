package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import org.junit.Test

class HasTumorMutationalLoadWithinRangeTest {

    @Test
    fun canEvaluate() {
        val function = HasTumorMutationalLoadWithinRange(140, null)
        val function2 = HasTumorMutationalLoadWithinRange(140, 280)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestMolecularTestFactory.withTumorMutationalLoad(null)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestMolecularTestFactory.withTumorMutationalLoad(200)))
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(TestMolecularTestFactory.withTumorMutationalLoad(200)))
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(TestMolecularTestFactory.withTumorMutationalLoad(280)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function2.evaluate(TestMolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(136, true, true))
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function2.evaluate(
                TestMolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    false
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function2.evaluate(
                TestMolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    true
                )
            )
        )
    }
}