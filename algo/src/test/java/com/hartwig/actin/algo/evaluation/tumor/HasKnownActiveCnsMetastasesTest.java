package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownActiveCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownActiveCnsMetastases function = new HasKnownActiveCnsMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveCnsLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveCnsLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveCnsLesions(null)));
    }
}