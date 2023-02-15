package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLungMetastasesTest {
    private final HasLungMetastases function = new HasLungMetastases();

    @Test
    public void shouldBeUndeterminedWhenHasLungLesionsIsNull() {
        Evaluation undetermined = function.evaluate(TumorTestFactory.withLungLesions(null));
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined);
    }

    @Test
    public void shouldPassWhenHasLungLesionsIsTrue() {
        Evaluation pass = function.evaluate(TumorTestFactory.withLungLesions(true));
        assertEvaluation(EvaluationResult.PASS, pass);
    }

    @Test
    public void shouldFailWhenHasLungLesionsIsFalse() {
        Evaluation fail = function.evaluate(TumorTestFactory.withLungLesions(false));
        assertEvaluation(EvaluationResult.FAIL, fail);
    }
}