package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Optional;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure;

import org.junit.Test;

public class ECGMeasureEvaluationFunctionTest {

    @Test
    public void evaluatesToUndeterminedWhenNoECGPresent() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        null)));
    }

    @Test
    public void evaluatesToUndeterminedWhenWrongUnit() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(400).unit("wrong unit").build())
                                .build())));
    }

    @Test
    public void maxThresholdCriteriaEvaluatesToPassWhenBelowThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    @Test
    public void maxThresholdCriteriaEvaluatesToPassWhenEqualThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    @Test
    public void maxThresholdCriteriaEvaluatesToFailWhenAboveThreshold() {
        assertEvaluation(EvaluationResult.FAIL,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    @Test
    public void minThresholdCriteriaEvaluatesToPassWhenAboveThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MINIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(500).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    @Test
    public void minThresholdCriteriaEvaluatesToPassWhenEqualThreshold() {
        assertEvaluation(EvaluationResult.PASS,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MINIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(450).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    @Test
    public void minThresholdCriteriaEvaluatesToFailWhenBelowThreshold() {
        assertEvaluation(EvaluationResult.FAIL,
                withThresholdCriteria(ECGMeasureEvaluationFunction.ThresholdCriteria.MINIMUM).evaluate(CardiacFunctionTestFactory.withECG(
                        CardiacFunctionTestFactory.builder()
                                .qtcfMeasure(ImmutableECGMeasure.builder().value(300).unit(ECGUnit.MILLISECONDS.symbol()).build())
                                .build())));
    }

    private static ECGMeasureEvaluationFunction withThresholdCriteria(
            final ECGMeasureEvaluationFunction.ThresholdCriteria thresholdCriteria) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.QTCF,
                450,
                ECGUnit.MILLISECONDS,
                e -> Optional.ofNullable(e.qtcfMeasure()),
                thresholdCriteria);
    }
}