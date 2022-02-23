package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownCnsMetastases function = new HasKnownCnsMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withCnsLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withCnsLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withCnsLesions(null)));
    }
}