package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownBrainMetastases function = new HasKnownBrainMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(null)));
    }
}