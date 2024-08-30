package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ECG
import com.hartwig.actin.datamodel.clinical.ECGMeasure
import org.junit.Test

class ECGMeasureEvaluationFunctionTest {

    @Test
    fun performNoEvaluationWhenNoECGPresent() {
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    null
                )
            )
        )
    }

    @Test
    fun evaluatesToUndeterminedWhenWrongUnit() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 400, unit = "wrong unit")
                    )
                )
            )
        )
    }

    @Test
    fun maxThresholdCriteriaEvaluatesToPassWhenBelowThreshold() {
        assertEvaluation(
            EvaluationResult.PASS,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 300, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    @Test
    fun maxThresholdCriteriaEvaluatesToPassWhenEqualThreshold() {
        assertEvaluation(
            EvaluationResult.PASS,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 450, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    @Test
    fun maxThresholdCriteriaEvaluatesToFailWhenAboveThreshold() {
        assertEvaluation(
            EvaluationResult.FAIL,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 500, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    @Test
    fun minThresholdCriteriaEvaluatesToPassWhenAboveThreshold() {
        assertEvaluation(
            EvaluationResult.PASS,
            withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 500, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    @Test
    fun minThresholdCriteriaEvaluatesToPassWhenEqualThreshold() {
        assertEvaluation(
            EvaluationResult.PASS,
            withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 450, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    @Test
    fun minThresholdCriteriaEvaluatesToFailWhenBelowThreshold() {
        assertEvaluation(
            EvaluationResult.FAIL,
            withThresholdCriteria(ThresholdCriteria.MINIMUM).evaluate(
                CardiacFunctionTestFactory.withECG(
                    CardiacFunctionTestFactory.createMinimal().copy(
                        qtcfMeasure = ECGMeasure(value = 300, unit = ECGUnit.MILLISECONDS.symbol())
                    )
                )
            )
        )
    }

    companion object {
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
}