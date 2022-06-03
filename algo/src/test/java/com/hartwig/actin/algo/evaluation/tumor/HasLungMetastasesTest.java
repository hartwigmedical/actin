package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLungMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLungMetastases function = new HasLungMetastases();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withLungLesions(null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLungLesions(false)));
    }
}