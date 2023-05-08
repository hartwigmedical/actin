package com.hartwig.actin.algo.evaluation.tumor

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasBoneMetastasesOnlyTest {
    @Test
    fun canEvaluate() {
        val function = HasBoneMetastasesOnly()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withBoneLesions(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withBoneLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, Lists.newArrayList())))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, Lists.newArrayList("skin")))
        )
    }
}