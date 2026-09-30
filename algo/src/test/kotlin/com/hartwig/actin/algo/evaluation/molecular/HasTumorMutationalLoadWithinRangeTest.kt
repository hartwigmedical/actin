package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasTumorMutationalLoadWithinRangeTest {

    @Test
    fun canEvaluate() {
        val function = HasTumorMutationalLoadWithinRange(140, null)
        val function2 = HasTumorMutationalLoadWithinRange(140, 280)
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)),
            "Undetermined if TML is sufficient (no TML result)"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)),
            "TML is above 140"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)),
            "TML is between 140 and 280"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(280)),
            "TML is between 140 and 280"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function2.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(136, true, true)),
            "TML 136 is not between 140 and 280"
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function2.evaluate(
                MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    false
                )
            ),
            "No molecular results of sufficient quality"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function2.evaluate(
                MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQualityAndPurity(
                    136,
                    false,
                    true
                )
            ),
            "TML 136 almost between 140 and 280 while purity is low - perhaps a few mutations are missed"
        )
    }
}