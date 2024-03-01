package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasLiverMetastasesOnlyTest {

    val function = HasLiverMetastasesOnly()

    @Test
    fun `Should pass when patient has liver metastases only`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverAndOtherLesions(true, emptyList())))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(false, true)))
    }

    @Test
    fun `Should evaluate to undetermined when data regarding liver metastases is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withLiverLesions(null)))
    }

    @Test
    fun `Should warn if patient has liver metastases but data regarding other lesions is missing `() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withLiverLesions(true)))
    }

    @Test
    fun `Should fail when patient does not have liver metastases exclusively`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLiverAndOtherLesions(true, listOf("skin"))))
    }

    @Test
    fun `Should fail when patient has no liver metastases`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLiverLesions(false)))
    }
}