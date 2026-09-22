package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasQTCFWithGenderTest {

    private val hasQTCFOfAtLeastWithGenderFunction =
        HasQtcfWithGender(450.0, Gender.MALE, HeartMeasurementEvaluationFunctions::hasSufficientQtcf)
    private val hasQTCFOfAtMostWithGenderFunction =
        HasQtcfWithGender(450.0, Gender.MALE, HeartMeasurementEvaluationFunctions::hasLimitedQtcf)

    @Test
    fun `Should fail with incorrect gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)),
            "Male QTCF acceptable bound not applicable for female"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtMostWithGenderFunction.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)),
            "Male QTCF acceptable bound not applicable for female"
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val atLeastEvaluation = hasQTCFOfAtLeastWithGenderFunction.evaluate(CardiacFunctionTestFactory.withEcg(null))
        val atMostEvaluation = hasQTCFOfAtMostWithGenderFunction.evaluate(CardiacFunctionTestFactory.withEcg(null))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, atLeastEvaluation, "No QTCF interval known")
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, atMostEvaluation, "No QTCF interval known")
        assertThat(atLeastEvaluation.recoverable).isTrue()
        assertThat(atMostEvaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(400.0, "wrong unit")),
            "QTCF measure in wrong unit instead of required ms"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(400.0, "wrong unit")),
            "QTCF measure in wrong unit instead of required ms"
        )
    }

    @Test
    fun `Should pass when QTCF above min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(500.0)),
            "QTCF of 500.0 ms exceeds min threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when QTCF equals min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(450.0)),
            "QTCF of 450.0 ms exceeds min threshold of 450.0"
        )
    }

    @Test
    fun `Should fail when QTCF below min threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtLeastWithGenderFunction.evaluate(withValueAndUnit(300.0)),
            "QTCF of 300.0 ms is below or equal to min threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when QTCF below max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(300.0)),
            "QTCF of 300.0 ms does not exceed max threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when QTCF equals max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(450.0)),
            "QTCF of 450.0 ms does not exceed max threshold of 450.0"
        )
    }

    @Test
    fun `Should fail when QTCF above max threshold and correct gender`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            hasQTCFOfAtMostWithGenderFunction.evaluate(withValueAndUnit(500.0)),
            "QTCF of 500.0 ms is above or equal to max threshold of 450.0"
        )
    }
}