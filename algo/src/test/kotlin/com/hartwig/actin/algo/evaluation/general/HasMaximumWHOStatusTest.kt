package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMaximumWHOStatusTest {

    private val function: HasMaximumWHOStatus = HasMaximumWHOStatus(2)

    @Test
    fun shouldReturnUndeterminedWhenWHOIsNull() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
    }

    @Test
    fun shouldPassWhenWHOIsLessThanOrEqualToMaximum() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }

    @Test
    fun shouldFailWhenWHOIsGreaterThanMaximum() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(3)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }

    @Test
    fun shouldWarnWhenWHOIsExactMatchAndPatientHasComplicationCategoriesOfConcern() {
        val evaluation: Evaluation = function.evaluate(GeneralTestFactory.withWHOAndComplications(2, listOf("Pleural Effusions")))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertTrue(
            evaluation.warnSpecificMessages()
                .contains(
                    "Patient WHO status 2 equals maximum but patient has complication categories of concern: Pleural Effusions" +
                            ", potentially indicating deterioration"
                )
        )
    }
}