package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.junit.Test

class HasSpecificMetastasesOnlyTest {

    private val hasLiverMetastasesOnly =
        HasSpecificMetastasesOnly(TumorDetails::hasLiverLesions, TumorDetails::hasSuspectedLiverLesions, "liver")
    private val hasBoneMetastasesOnly =
        HasSpecificMetastasesOnly(TumorDetails::hasBoneLesions, TumorDetails::hasSuspectedBoneLesions, "bone")

    @Test
    fun `Should pass when patient has liver metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withLiverAndOtherLesions(true, emptyList()))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withTumorDetails(createDetails(false, true)))
        )
    }

    @Test
    fun `Should warn when patient has suspected liver metastases only`() {
        assertEvaluation(
            EvaluationResult.WARN,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withBoneAndSuspectedLiverLesions(false, true))
        )
    }

    @Test
    fun `Should evaluate to undetermined when data regarding liver metastases is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, hasLiverMetastasesOnly.evaluate(TumorTestFactory.withLiverLesions(null)))
    }

    @Test
    fun `Should warn if patient has liver metastases but data regarding any other lesion is missing`() {
        assertEvaluation(EvaluationResult.WARN, hasLiverMetastasesOnly.evaluate(TumorTestFactory.withLiverLesions(true)))
        assertEvaluation(EvaluationResult.WARN, hasLiverMetastasesOnly.evaluate(TumorTestFactory.withBoneAndLiverLesions(null, true)))
    }

    @Test
    fun `Should fail when patient does not have liver metastases exclusively`() {
        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withLiverAndOtherLesions(true, listOf("skin")))
        )
    }

    @Test
    fun `Should fail when patient has no liver metastases`() {
        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(TumorTestFactory.withLiverLesions(false)))
    }

    @Test
    fun `Should pass when patient has bone metastases only`() {
        assertEvaluation(EvaluationResult.PASS, hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, emptyList())))
        assertEvaluation(
            EvaluationResult.PASS,
            hasBoneMetastasesOnly.evaluate(TumorTestFactory.withTumorDetails(createDetails(true, false)))
        )
    }

    @Test
    fun `Should warn when patient has suspected bone metastases only`() {
        assertEvaluation(
            EvaluationResult.WARN,
            hasBoneMetastasesOnly.evaluate(TumorTestFactory.withSuspectedBoneAndOtherLesions(true, emptyList()))
        )
    }

    @Test
    fun `Should evaluate to undetermined when data regarding bone metastases is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneLesions(null)))
    }

    @Test
    fun `Should warn if patient has bone metastases but data regarding other lesions is missing `() {
        assertEvaluation(EvaluationResult.WARN, hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneLesions(true)))
    }

    @Test
    fun `Should fail when patient does not have bone metastases exclusively`() {
        assertEvaluation(EvaluationResult.FAIL, hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneAndLiverLesions(true, true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneAndOtherLesions(true, listOf("skin")))
        )
    }

    @Test
    fun `Should fail when patient has no bone metastases`() {
        assertEvaluation(EvaluationResult.FAIL, hasBoneMetastasesOnly.evaluate(TumorTestFactory.withBoneLesions(false)))
    }

    private fun createDetails(hasBoneMetastases: Boolean, hasLiverMetastases: Boolean): TumorDetails {
        return TumorDetails(
            hasBoneLesions = hasBoneMetastases,
            hasLiverLesions = hasLiverMetastases,
            hasBrainLesions = false,
            hasCnsLesions = false,
            hasLymphNodeLesions = false,
            hasLungLesions = false
        )
    }
}