package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasNormalCardiacFunctionByMUGAOrTTETest {

    @Test
    public void canEvaluate() {
        HasNormalCardiacFunctionByMUGAOrTTE function = new HasNormalCardiacFunctionByMUGAOrTTE();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(CardiacFunctionTestFactory.withLVEF(null)));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(CardiacFunctionTestFactory.withLVEF(0.8)));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(CardiacFunctionTestFactory.withLVEF(0.3)));
    }
}