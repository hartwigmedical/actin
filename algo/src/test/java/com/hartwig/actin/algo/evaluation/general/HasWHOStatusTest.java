package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasWHOStatusTest {

    @Test
    public void canEvaluate() {
        HasWHOStatus function = new HasWHOStatus(2);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(0)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)));
    }
}