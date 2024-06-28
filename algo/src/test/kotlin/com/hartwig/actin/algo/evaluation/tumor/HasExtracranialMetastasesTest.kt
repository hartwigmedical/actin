package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TumorDetails
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
    fun `Should pass when uncategorized lesion with certain extracranial location present`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(listOf("intestine"))))
    }

    @Test
    fun `Should pass for biopsy location with certain extracranial location`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                hasBoneLesions = false,
                hasLymphNodeLesions = false,
                hasLiverLesions = false,
                hasLungLesions = false,
                hasCnsLesions = false,
                hasBrainLesions = false,
                otherLesions = emptyList(),
                biopsyLocation = "bladder"
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when only uncategorized lesions with uncertain extracranial location present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherLesions(listOf("gland")))
        )
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
    fun `Should evaluate to undetermined when data is missing for one of the lesion categories`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                hasBoneLesions = null,
                hasLymphNodeLesions = false,
                hasLiverLesions = false,
                hasLungLesions = false,
                hasCnsLesions = false,
                hasBrainLesions = false,
                otherLesions = emptyList(),
                biopsyLocation = ""
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail when only brain metastases present`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                hasBoneLesions = false,
                hasLymphNodeLesions = false,
                hasLiverLesions = false,
                hasLungLesions = false,
                hasCnsLesions = false,
                hasBrainLesions = true,
                otherLesions = emptyList(),
                biopsyLocation = ""
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when no metastases present`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                hasBoneLesions = false,
                hasLymphNodeLesions = false,
                hasLiverLesions = false,
                hasLungLesions = false,
                hasCnsLesions = false,
                hasBrainLesions = false,
                otherLesions = emptyList(),
                biopsyLocation = ""
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }
}