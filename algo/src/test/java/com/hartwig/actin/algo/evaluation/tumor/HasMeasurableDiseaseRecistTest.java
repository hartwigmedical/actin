package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMeasurableDiseaseRecistTest {

    @Test
    public void canEvaluate() {
        HasMeasurableDiseaseRecist function = new HasMeasurableDiseaseRecist();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDisease(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableDisease(false)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableDisease(null)));
    }
}