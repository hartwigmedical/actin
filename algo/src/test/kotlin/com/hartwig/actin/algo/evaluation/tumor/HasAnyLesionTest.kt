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
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withCnsLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLymphNodeLesions(true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("other"))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLiverLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withCnsLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLungLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLymphNodeLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList())))
    }
}