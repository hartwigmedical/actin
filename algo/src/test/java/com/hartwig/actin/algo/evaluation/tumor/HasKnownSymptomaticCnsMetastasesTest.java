package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownSymptomaticCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownSymptomaticCnsMetastases function = new HasKnownSymptomaticCnsMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withSymptomaticCnsLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withSymptomaticCnsLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withSymptomaticCnsLesions(null)));
    }
}