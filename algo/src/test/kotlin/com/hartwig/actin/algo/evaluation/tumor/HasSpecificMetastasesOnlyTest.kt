package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import org.junit.Test

class HasSpecificMetastasesOnlyTest {

    private val hasLiverMetastasesOnly = HasSpecificMetastasesOnly(TumorDetails::hasLiverLesions, "liver")
    private val hasBoneMetastasesOnly = HasSpecificMetastasesOnly(TumorDetails::hasBoneLesions, "bone")

    @Test
    fun `Should pass when patient has liver metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverMetastasesOnly.evaluate(TestTumorFactory.withLiverAndOtherLesions(true, emptyList()))
        )
        assertEvaluation(EvaluationResult.PASS, hasLiverMetastasesOnly.evaluate(TestTumorFactory.withBoneAndLiverLesions(false, true)))
    }

    @Test
    fun `Should evaluate to undetermined when data regarding liver metastases is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, hasLiverMetastasesOnly.evaluate(TestTumorFactory.withLiverLesions(null)))
    }

    @Test
    fun `Should warn if patient has liver metastases but data regarding other lesions is missing `() {
        assertEvaluation(EvaluationResult.WARN, hasLiverMetastasesOnly.evaluate(TestTumorFactory.withLiverLesions(true)))
    }

    @Test
    fun `Should fail when patient does not have liver metastases exclusively`() {
        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(TestTumorFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(TestTumorFactory.withLiverAndOtherLesions(true, listOf("skin")))
        )
    }

    @Test
    fun `Should fail when patient has no liver metastases`() {
        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(TestTumorFactory.withLiverLesions(false)))
    }

    @Test
    fun `Should pass when patient has bone metastases only`() {
        assertEvaluation(EvaluationResult.PASS, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneAndOtherLesions(true, emptyList())))
        assertEvaluation(EvaluationResult.PASS, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneAndLiverLesions(true, false)))
    }

    @Test
    fun `Should evaluate to undetermined when data regarding bone metastases is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneLesions(null)))
    }

    @Test
    fun `Should warn if patient has bone metastases but data regarding other lesions is missing `() {
        assertEvaluation(EvaluationResult.WARN, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneLesions(true)))
    }

    @Test
    fun `Should fail when patient does not have bone metastases exclusively`() {
        assertEvaluation(EvaluationResult.FAIL, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneAndOtherLesions(true, listOf("skin")))
        )
    }

    @Test
    fun `Should fail when patient has no bone metastases`() {
        assertEvaluation(EvaluationResult.FAIL, hasBoneMetastasesOnly.evaluate(TestTumorFactory.withBoneLesions(false)))
    }
}