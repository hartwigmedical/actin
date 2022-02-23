package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownActiveBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownActiveBrainMetastases function = new HasKnownActiveBrainMetastases();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainLesions(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainLesions(false)));
    }
}