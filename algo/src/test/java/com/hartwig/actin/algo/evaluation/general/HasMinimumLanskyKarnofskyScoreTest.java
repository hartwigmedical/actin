package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMinimumLanskyKarnofskyScoreTest {

    @Test
    public void canEvaluate() {
        HasMinimumLanskyKarnofskyScore function = new HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 70);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(0)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(1)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(2)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(3)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(5)));

        HasMinimumLanskyKarnofskyScore function2 = new HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, 80);

        assertEvaluation(EvaluationResult.PASS, function2.evaluate(GeneralTestFactory.withWHO(0)));
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(GeneralTestFactory.withWHO(1)));
        assertEvaluation(EvaluationResult.WARN, function2.evaluate(GeneralTestFactory.withWHO(2)));
        assertEvaluation(EvaluationResult.FAIL, function2.evaluate(GeneralTestFactory.withWHO(3)));
    }
}