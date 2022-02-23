package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLiverMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLiverMetastases function = new HasLiverMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLiverLesions(false)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withLiverLesions(null)));
    }
}