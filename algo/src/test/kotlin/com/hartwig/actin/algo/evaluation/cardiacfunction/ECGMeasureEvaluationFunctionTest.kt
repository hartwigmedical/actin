package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ECG
import org.junit.Assert.assertTrue
import org.junit.Test

class ECGMeasureEvaluationFunctionTest {

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
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
        assertEvaluation(EvaluationResult.PASS, withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(300)))
    }

    @Test
    fun `Should pass when value equals max threshold`() {
        assertEvaluation(EvaluationResult.PASS, withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when value above max threshold`() {
        assertEvaluation(EvaluationResult.FAIL, withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(500)))
    }

    @Test
    fun `Should pass when value above min threshold`() {
        assertEvaluation(EvaluationResult.PASS, withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(500)))
    }

    @Test
    fun `Should pass when value equals min threshold`() {
        assertEvaluation(EvaluationResult.PASS, withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(450)))
    }

    @Test
    fun `Should fail when value below min threshold`() {
        assertEvaluation(EvaluationResult.FAIL, withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(withValueAndUnit(300)))
    }

    private fun withThresholdCriteria(
        thresholdCriteria: ThresholdCriteria
    ): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            450.0,
            ECGUnit.MILLISECONDS,
            ECG::qtcfMeasure,
            thresholdCriteria
        )
    }
}