package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasWHOStatusTest {

    private val function = HasWHOStatus(2)

    @Test
    fun shouldReturnUndeterminedWhenWHOIsNull() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
    }

    @Test
    fun shouldFailWhenWHODifferenceIsGreaterThanOne() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }

    @Test
    fun shouldReturnRecoverableFailWhenWHODifferenceIsExactlyOne() {
        val evaluationFor1 = function.evaluate(GeneralTestFactory.withWHO(1))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor1)
        assertTrue(evaluationFor1.recoverable())

        val evaluationFor3 = function.evaluate(GeneralTestFactory.withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor3)
        assertTrue(evaluationFor3.recoverable())
    }

    @Test
    fun shouldPassWhenWHOIsExactMatch() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }

    @Test
    fun shouldWarnWhenWHOIsExactMatchAndPatientHasComplicationCategoriesOfConcern() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHOAndComplications(2, listOf("Pleural Effusions")))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertTrue(
            evaluation.warnSpecificMessages()
                .contains(
                    "Patient WHO status 2 matches requested but patient has complication categories of concern: " +
                            "Pleural Effusions, potentially indicating deterioration"
                )
        )
    }
}