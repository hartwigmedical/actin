package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ECG
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ECGMeasureEvaluationFunctionTest {

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(400, "wrong unit"))
        )
    }

    @Test
    fun `Should pass when value below max threshold`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(300))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 300 ms does not exceed max threshold of 450.0")
    }

    @Test
    fun `Should pass when value equals max threshold`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(450))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 450 ms does not exceed max threshold of 450.0")
    }

    @Test
    fun `Should fail when value above max threshold`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(500))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("QTCF of 500 ms is above or equal to max threshold of 450.0")
    }

    @Test
    fun `Should pass when value above min threshold`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(500))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 500 ms exceeds min threshold of 450.0")
    }

    @Test
    fun `Should pass when value equals min threshold`() {
        val evaluation =  withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(450))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 450 ms exceeds min threshold of 450.0")
    }

    @Test
    fun `Should fail when value below min threshold`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(300))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("QTCF of 300 ms is below or equal to min threshold of 450.0")
    }

    private fun withThresholdCriteria(thresholdCriteria: ThresholdCriteria): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            450.0,
            ECGUnit.MILLISECONDS,
            ECG::qtcfMeasure,
            thresholdCriteria
        )
    }
}