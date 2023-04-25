package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownCnsMetastases function = new HasKnownCnsMetastases();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(false, null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(false, false)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(true, null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(false, true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(true, false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainAndCnsLesions(true, true)));
    }
}