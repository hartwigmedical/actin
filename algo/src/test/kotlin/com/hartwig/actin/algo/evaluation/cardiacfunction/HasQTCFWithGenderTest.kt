package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasQTCFWithGenderTest {

    private val hasQTCFOfAtLeastWithGenderFunction = HasQTCFWithGender(450.0, Gender.MALE, ECGMeasureEvaluationFunctions::hasSufficientQTCF)
    private val hasQTCFOfAtMostWithGenderFunction = HasQTCFWithGender(450.0, Gender.MALE, ECGMeasureEvaluationFunctions::hasLimitedQTCF)

    @Test
    fun `Should fail with incorrect gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(GeneralTestFactory.withGender(Gender.FEMALE))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtMostWithGenderFunction.evaluate(GeneralTestFactory.withGender(Gender.FEMALE))
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val atLeastEvaluation = hasQTCFOfAtLeastWithGenderFunction.evaluate(CardiacFunctionTestFactory.withECG(null))
        val atMostEvaluation = hasQTCFOfAtMostWithGenderFunction.evaluate(CardiacFunctionTestFactory.withECG(null))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, atLeastEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, atMostEvaluation)
        assertThat(atLeastEvaluation.recoverable).isTrue()
        assertThat(atMostEvaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(400, "wrong unit"))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(400, "wrong unit"))
        )
    }

    @Test
    fun `Should pass when QTCF above min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(500)))
    }

    @Test
    fun `Should pass when QTCF equals min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when QTCF below min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(300)))
    }

    @Test
    fun `Should pass when QTCF below max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(300)))
    }

    @Test
    fun `Should pass when QTCF equals max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when QTCF above max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(500)))
    }
}