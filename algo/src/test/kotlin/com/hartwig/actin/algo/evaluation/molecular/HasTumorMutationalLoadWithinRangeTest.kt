package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasTumorMutationalLoadWithinRangeTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).molecular

    @Test
    fun canEvaluate() {
        val function = HasTumorMutationalLoadWithinRange(140, null, labels)
        val function2 = HasTumorMutationalLoadWithinRange(140, 280, labels)
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