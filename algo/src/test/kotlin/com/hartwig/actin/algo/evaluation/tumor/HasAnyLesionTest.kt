package com.hartwig.actin.algo.evaluation.tumor

import com.google.common.collect.Lists
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasAnyLesionTest {
    @Test
    fun `Should fail if patient has no lesions`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should evaluate to pass if only bone lesions are present only`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only liver lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only CNS lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withCnsLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only brain lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only lung lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only lymph node lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLymphNodeLesions(true)))
    }

    @Test
    fun `Should evaluate to pass if only other lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("other"))))
    }

    @Test
    fun `Should evaluate to undetermined if only suspected lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }

    @Test
    fun `Should evaluate to undetermined if no lesions and only suspected lesions are present`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }

    @Test
    fun `Should evaluate to undetermined if only suspected lesions are present (message)`() {
        val function = HasAnyLesion()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }
}