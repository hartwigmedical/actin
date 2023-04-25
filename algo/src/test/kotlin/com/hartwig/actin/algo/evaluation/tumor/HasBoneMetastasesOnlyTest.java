package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasBoneMetastasesOnlyTest {

    @Test
    public void canEvaluate() {
        HasBoneMetastasesOnly function = new HasBoneMetastasesOnly();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withBoneLesions(null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withBoneLesions(true)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, true)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, Lists.newArrayList())));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, Lists.newArrayList("skin"))));
    }
}