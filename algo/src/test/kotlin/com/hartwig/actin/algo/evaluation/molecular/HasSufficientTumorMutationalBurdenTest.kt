package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import org.junit.Test

class HasSufficientTumorMutationalBurdenTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientTumorMutationalBurden(10.0)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestMolecularTestFactory.withTumorMutationalBurden(null)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestMolecularTestFactory.withTumorMutationalBurden(20.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestMolecularTestFactory.withTumorMutationalBurden(10.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestMolecularTestFactory.withTumorMutationalBurden(1.0)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestMolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    true,
                    true
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestMolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    false
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TestMolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    true
                )
            )
        )
    }
}