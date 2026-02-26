package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.junit.jupiter.api.Test

class HasSevereConcomitantIllnessTest {
    val function = HasSevereConcomitantIllness()

    @Test
    fun `Should not evaluate when WHO unknown`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(null)))
    }

    @Test
    fun `Should not evaluate when WHO 2 or less`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(0)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(1)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(2)))
    }

    @Test
    fun `Should not evaluate when WHO is at most 2 or less`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(0, WhoStatusPrecision.AT_MOST)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(1, WhoStatusPrecision.AT_MOST)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withWHO(2, WhoStatusPrecision.AT_MOST)))
    }

    @Test
    fun `Should warn when WHO 3 or 4`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(4)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(3)))
    }

    @Test
    fun `Should pass when WHO 5`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(5)))
    }

    @Test
    fun `Should return undetermined when WHO is not an exact value`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withWHO(1, WhoStatusPrecision.AT_LEAST))
        )
    }
}