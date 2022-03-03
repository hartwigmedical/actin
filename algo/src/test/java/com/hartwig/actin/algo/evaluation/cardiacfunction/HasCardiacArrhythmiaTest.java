package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;

import org.junit.Test;

public class HasCardiacArrhythmiaTest {

    @Test
    public void canEvaluateWithoutType() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia(null);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true)));
    }

    @Test
    public void canEvaluateWithType() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia("serious");

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(CardiacFunctionTestFactory.withECG(ImmutableECG.builder()
                        .hasSigAberrationLatestECG(true)
                        .aberrationDescription("Fine condition")
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(CardiacFunctionTestFactory.withECG(ImmutableECG.builder()
                        .hasSigAberrationLatestECG(true)
                        .aberrationDescription("Serious condition")
                        .build())));
    }
}