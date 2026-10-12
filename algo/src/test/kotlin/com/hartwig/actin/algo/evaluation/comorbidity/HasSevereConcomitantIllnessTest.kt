package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.junit.jupiter.api.Test

class HasSevereConcomitantIllnessTest {

    val function = HasSevereConcomitantIllness()

    @Test
    fun `Should warn when WHO is at least 3`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(3)), "Potentially has severe concomitant illnesses (WHO 3)")
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withWHO(4)), "Potentially has severe concomitant illnesses (WHO 4)")
    }

    @Test
    fun `Should warn when WHO at least 3`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(withWHO(3, precision = WhoStatusPrecision.AT_LEAST)),
            "Potentially has severe concomitant illnesses (WHO >=3)"
        )
    }

    @Test
    fun `Should ignore isAtLeast evaluations with 'undetermined' outcome and fail`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(2, precision = WhoStatusPrecision.AT_LEAST)),
            "Assumed that patient has no severe concomitant illnesses"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(3, precision = WhoStatusPrecision.AT_MOST)),
            "Assumed that patient has no severe concomitant illnesses"
        )
    }

    @Test
    fun `Should fail when WHO is 2`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(2)), "Assumed that patient has no severe concomitant illnesses")
    }

    @Test
    fun `Should fail when WHO missing`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(null)),
            "Assumed that patient has no severe concomitant illnesses"
        )
    }
}