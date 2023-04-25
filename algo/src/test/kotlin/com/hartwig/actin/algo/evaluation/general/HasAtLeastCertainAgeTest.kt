package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasAtLeastCertainAgeTest {

    @Test
    public void canEvaluate() {
        HasAtLeastCertainAge function = new HasAtLeastCertainAge(2020, 18);

        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withBirthYear(1960)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withBirthYear(2014)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withBirthYear(2002)));
    }
}