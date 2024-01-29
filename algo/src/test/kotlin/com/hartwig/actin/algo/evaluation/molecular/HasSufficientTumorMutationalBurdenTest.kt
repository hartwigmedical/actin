package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import org.junit.Test

class HasSufficientTumorMutationalBurdenTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientTumorMutationalBurden(10.0)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(null)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1.0)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    true,
                    true
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    false
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    true
                )
            )
        )
    }
}