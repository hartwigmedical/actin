package com.hartwig.actin.algo.evaluation.tumor

import com.google.common.collect.Lists
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasAnyLesionTest {
    @Test
    fun canEvaluate() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBoneLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withLiverLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withCnsLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withLungLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withLymphNodeLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withOtherLesions(Lists.newArrayList("other"))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBoneLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withLiverLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withCnsLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withLungLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withLymphNodeLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withOtherLesions(Lists.newArrayList())))
    }
}