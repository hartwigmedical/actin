package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSufficientTumorMutationalBurdenTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientTumorMutationalBurden(10.0)
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withTumorMutationalBurden(null)),
            "Undetermined if TMB is above 10.0 (no TMB result)"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20.0)),
            "TMB is above 10.0"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10.0)),
            "TMB is above 10.0"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1.0)),
            "TMB 1.0 is not above 10.0"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    true,
                    true
                )
            ),
            "TMB 9.5 is not above 10.0"
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    false
                )
            ),
            "No molecular results of sufficient quality"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
                    9.5,
                    false,
                    true
                )
            ),
            "TMB 9.5 almost exceeds min TMB 10.0 while purity is low - perhaps a few mutations are missed"
        )
    }
}