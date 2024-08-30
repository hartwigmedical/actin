package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasMinimumLanskyKarnofskyScoreTest {

    @Test
    fun canEvaluate() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 70)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(0)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(1)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(2)))
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(GeneralTestFactory.withWHO(3)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(5)))

        val function2 = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function2.evaluate(GeneralTestFactory.withWHO(0)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function2.evaluate(GeneralTestFactory.withWHO(1)))
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function2.evaluate(GeneralTestFactory.withWHO(2)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function2.evaluate(GeneralTestFactory.withWHO(3)))
    }
}