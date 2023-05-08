package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Assert
import org.junit.Test

class HasWHOStatusTest {
    private val function = HasWHOStatus(2)

    @Test
    fun shouldReturnUndeterminedWhenWHOIsNull() {
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
    }

    @Test
    fun shouldFailWhenWHODifferenceIsGreaterThanOne() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(0)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }

    @Test
    fun shouldWarnWhenWHODifferenceIsExactlyOne() {
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(GeneralTestFactory.withWHO(1)))
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(GeneralTestFactory.withWHO(3)))
    }

    @Test
    fun shouldPassWhenWHOIsExactMatch() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }

    @Test
    fun shouldWarnWhenWHOIsExactMatchAndPatientHasComplicationCategoriesOfConcern() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHOAndComplications(2, listOf("Pleural Effusions")))
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, evaluation)
        Assert.assertTrue(
            evaluation.warnSpecificMessages()
                .contains("Patient WHO status 2 matches requested but patient has complication categories of concern: Pleural Effusions")
        )
    }
}