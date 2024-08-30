package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadAnyCancerTreatmentTest {

    private val functionWithoutCategoryToIgnore = HasHadAnyCancerTreatment(null)
    private val functionWithCategoryToIgnore = HasHadAnyCancerTreatment(TreatmentCategory.CHEMOTHERAPY)

    @Test
    fun `Should fail when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should pass if treatment history is not empty and contains treatments which should not be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Radiotherapy", true, setOf(TreatmentCategory.RADIOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should fail if treatment history contains only treatments which should be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }
}