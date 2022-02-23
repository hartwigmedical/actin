package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMeasurableDiseaseRecistTest {

    @Test
    public void canEvaluate() {
        HasMeasurableDiseaseRecist function = new HasMeasurableDiseaseRecist();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableLesionRecist(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableLesionRecist(false)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableLesionRecist(null)));
    }
}