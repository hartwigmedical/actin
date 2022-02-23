package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasBoneMetastasesTest {

    @Test
    public void canEvaluate() {
        HasBoneMetastases function = new HasBoneMetastases();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withBoneLesions(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)));
    }
}