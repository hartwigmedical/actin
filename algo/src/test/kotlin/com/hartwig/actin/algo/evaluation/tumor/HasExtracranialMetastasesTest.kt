package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasExtracranialMetastasesTest {

    private val function = HasExtracranialMetastases()

    @Test
    fun `Should pass when non-cns categorized metastases present`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(true)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(true)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(true)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLymphNodeLesions(true)))
    }

    @Test
    fun `Should evaluate to undetermined when only cns metastases present`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withCnsLesions(true)))
    }

    @Test
    fun `Should evaluate to undetermined when only uncategorized metastases present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherLesions(listOf("unknown site")))
        )
    }

    @Test
    fun `Should fail when only brain metastases present`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(true)))
    }

    @Test
    fun `Should fail when no metastases present`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)))
    }
}