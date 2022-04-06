package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownActiveCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownActiveCnsMetastases function = new HasKnownActiveCnsMetastases();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(null, null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(null, false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(false, null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(false, false)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(null, true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(true, null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(false, true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(true, false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveBrainAndCnsLesions(true, true)));
    }
}