package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure
import org.junit.Test

class ECGMeasureEvaluationFunctionTest {
    @Test
    fun evaluatesToUndeterminedWhenNoECGPresent() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(400).unit("wrong unit").build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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
                    CardiacFunctionTestFactory.builder()
                        .qtcfMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnit.MILLISECONDS.symbol()).build())
                        .build()
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