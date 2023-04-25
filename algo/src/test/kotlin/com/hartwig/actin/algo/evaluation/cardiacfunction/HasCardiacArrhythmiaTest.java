package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasCardiacArrhythmiaTest {

    @Test
    public void canEvaluate() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, "with description")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, null)));
    }
}