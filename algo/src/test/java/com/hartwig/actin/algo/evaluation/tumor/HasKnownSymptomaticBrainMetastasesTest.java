package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownSymptomaticBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownSymptomaticBrainMetastases function = new HasKnownSymptomaticBrainMetastases();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withSymptomaticBrainLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withSymptomaticBrainLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withSymptomaticBrainLesions(null)));
    }
}