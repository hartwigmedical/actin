package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSevereConcomitantIllnessTest {

    val function = HasSevereConcomitantIllness()

    @Test
    fun `Should warn when WHO at least 3`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(3)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(4)))
    }

    @Test
    fun `Should fail when WHO at most 2 or unknown`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(2)))
    }

    @Test
    fun `Should fail when WHO missing`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(null)))
    }
}