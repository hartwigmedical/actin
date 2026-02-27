package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasTumorMutationalLoadWithinRangeTest {

    @Test
    fun canEvaluate() {
        val function = HasTumorMutationalLoadWithinRange(140, null)
        val function2 = HasTumorMutationalLoadWithinRange(140, 280)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)))
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)))
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(280)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function2.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(136, true, true))
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function2.evaluate(
                MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    false
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function2.evaluate(
                MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    true
                )
            )
        )
    }
}