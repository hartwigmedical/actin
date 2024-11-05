package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.junit.Assert
import org.junit.Test

class HasQTCFOfAtMostWithGenderTest {

    private val function = HasQTCFOfAtMostWithGender(450.0, Gender.MALE)

    @Test
    fun `Should fail with incorrect gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)))
    }

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val evaluation = function.evaluate(CardiacFunctionTestFactory.withECG(null))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assert.assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(CardiacFunctionTestFactory.withValueAndUnit(400, "wrong unit"))
        )
    }

    @Test
    fun `Should pass when QTCF below max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withValueAndUnit(300)))
    }

    @Test
    fun `Should pass when QTCF equals max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when QTCF above max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withValueAndUnit(500)))
    }
}