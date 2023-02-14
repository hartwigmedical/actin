package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLiverMetastasesTest {
    private final HasLiverMetastases function = new HasLiverMetastases();

    @Test
    public void shouldBeUndeterminedWhenHasLiverLesionsIsNull() {
        Evaluation undetermined = function.evaluate(TumorTestFactory.withLiverLesions(null));
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined);
    }

    @Test
    public void shouldPassWhenHasLiverLesionsIsTrue() {
        Evaluation pass = function.evaluate(TumorTestFactory.withLiverLesions(true));
        assertEvaluation(EvaluationResult.PASS, pass);
    }

    @Test
    public void shouldFailWhenHasLiverLesionsIsFalse() {
        Evaluation fail = function.evaluate(TumorTestFactory.withLiverLesions(false));
        assertEvaluation(EvaluationResult.FAIL, fail);
    }
}