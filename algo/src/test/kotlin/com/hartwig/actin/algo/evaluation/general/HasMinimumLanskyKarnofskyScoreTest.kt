package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasMinimumLanskyKarnofskyScoreTest {

    @Test
    fun `Should evaluate LANSKY performance based on different exact who values `() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 70)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withWHO(null)),
            "Undetermined if Lansky score based on WHO status is at least 70 (WHO data missing)"
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(0)), "Lansky score based on WHO status is at least 70")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(1)), "Lansky score based on WHO status is at least 70")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(2)), "Undetermined if Lansky score is at least 70")
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(3)),
            "Lansky score based on WHO status below requested score of 70"
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(4)), "Lansky score based on WHO status is below 70")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(5)), "Lansky score based on WHO status is below 70")

        val function2 = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(withWHO(0)), "Lansky score based on WHO status is at least 80")
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(withWHO(1)), "Lansky score based on WHO status is at least 80")
        assertEvaluation(
            EvaluationResult.FAIL,
            function2.evaluate(withWHO(2)),
            "Lansky score based on WHO status below requested score of 80"
        )
        assertEvaluation(EvaluationResult.FAIL, function2.evaluate(withWHO(3)), "Lansky score based on WHO status is below 80")
    }

    @Test
    fun `Should evaluate LANSKY performance based on different maximum (at most) who values`() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        listOf(0,1).forEach {
            assertEvaluation(
                EvaluationResult.PASS,
                function.evaluate(withWHO(it, WhoStatusPrecision.AT_MOST)),
                "Lansky score based on WHO status is at least 80"
            )
        }
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withWHO(2, WhoStatusPrecision.AT_MOST)),
            "Undetermined if Lansky score is at least 80"
        )
    }

    @Test
    fun `Should evaluate LANSKY performance based on different minimum (at least) who values`() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        listOf(0, 1).forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(withWHO(it, WhoStatusPrecision.AT_LEAST)),
                "Undetermined if Lansky score is at least 80"
            )
        }
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(2, WhoStatusPrecision.AT_LEAST)),
            "Lansky score based on WHO status below requested score of 80"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(3, WhoStatusPrecision.AT_LEAST)),
            "Lansky score based on WHO status is below 80"
        )
    }

    @Test
    fun `Should be recoverable fail when WHO difference is exactly one`() {
        val function = HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80)
        val evaluation = function.evaluate(withWHO(2, WhoStatusPrecision.AT_LEAST))
        assertEvaluation(EvaluationResult.FAIL, evaluation, "Lansky score based on WHO status below requested score of 80")
        assertThat(evaluation.recoverable).isTrue()
    }
}
