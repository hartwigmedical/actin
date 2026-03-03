package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.junit.jupiter.api.Test

class HasMinimumLanskyKarnofskyScoreTest {

    @Test
    fun `Should evaluate LANSKY performance based on different exact who values `() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 70)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(1)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(2)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(3)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(4)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(5)))

        val function2 = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(withWHO(0)))
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(withWHO(1)))
        assertEvaluation(EvaluationResult.WARN, function2.evaluate(withWHO(2)))
        assertEvaluation(EvaluationResult.FAIL, function2.evaluate(withWHO(3)))
    }

    @Test
    fun `Should evaluate LANSKY performance based on different maximum (at most) who values`() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(0, WhoStatusPrecision.AT_MOST)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(1, WhoStatusPrecision.AT_MOST)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(2, WhoStatusPrecision.AT_MOST)))
    }

    @Test
    fun `Should evaluate LANSKY performance based on different minimum (at least) who values`() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(0, WhoStatusPrecision.AT_LEAST)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(1, WhoStatusPrecision.AT_LEAST)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(2, WhoStatusPrecision.AT_LEAST)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(3, WhoStatusPrecision.AT_LEAST)))
    }

}