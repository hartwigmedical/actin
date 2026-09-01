package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.junit.jupiter.api.Test

class HasExtracranialMetastasesTest {

    private val function = HasExtracranialMetastases()

    @Test
    fun `Should pass when non-cns categorized metastases present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBoneLesions(true)),
            "Extracranial metastases in provided metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLiverLesions(true)),
            "Extracranial metastases in provided metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLungLesions(true)),
            "Extracranial metastases in provided metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLymphNodeLesions(true)),
            "Extracranial metastases in provided metastases"
        )
    }

    @Test
    fun `Should pass when uncategorized lesion with certain extracranial location present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("intestine"))),
            "Extracranial metastases in provided metastases"
        )
    }

    @Test
    fun `Should evaluate to undetermined when only uncategorized lesions with uncertain extracranial location present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherLesions(listOf("gland"))),
            "Undetermined presence of extracranial metastases based on provided lesions"
        )
    }

    @Test
    fun `Should evaluate to undetermined when only uncategorized metastases present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherLesions(listOf("unknown site"))),
            "Undetermined presence of extracranial metastases based on provided lesions"
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
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record),
            "Undetermined presence of extracranial metastases based on provided lesions"
        )
    }

    @Test
    fun `Should warn when only suspected extracranial metastases present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withSuspectedBoneAndOtherLesions(true, listOf("unknown site"))),
            "Extracranial metastases in provided metastases but only suspected lesions"
        )
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
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record),
            "No extracranial metastases in provided lesions"
        )
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
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record),
            "No extracranial metastases in provided lesions"
        )
    }
}