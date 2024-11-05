package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.junit.Assert
import org.junit.Test

class HasQTCFOfAtLeastWithGenderTest {

    private val function = HasQTCFOfAtLeastWithGender(450.0, Gender.MALE)

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
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withValueAndUnit(400, "wrong unit")))
    }

    @Test
    fun `Should pass when QTCF above min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withValueAndUnit(500)))
    }

    @Test
    fun `Should pass when QTCF equals min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when QTCF below min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withValueAndUnit(300)))
    }
}