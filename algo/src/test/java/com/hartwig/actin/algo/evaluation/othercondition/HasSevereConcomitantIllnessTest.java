package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSevereConcomitantIllnessTest {

    @Test
    public void canEvaluate() {
        HasSevereConcomitantIllness function = new HasSevereConcomitantIllness();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(0)));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(OtherConditionTestFactory.withWHO(4)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(OtherConditionTestFactory.withWHO(null)));
    }
}