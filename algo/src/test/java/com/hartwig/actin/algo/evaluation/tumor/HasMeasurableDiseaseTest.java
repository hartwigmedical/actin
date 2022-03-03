package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMeasurableDiseaseTest {

    @Test
    public void canEvaluate() {
        HasMeasurableDisease function = new HasMeasurableDisease();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDisease(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableDisease(false)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableDisease(null)));
    }
}