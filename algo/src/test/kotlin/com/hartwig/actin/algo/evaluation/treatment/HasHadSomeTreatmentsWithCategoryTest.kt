package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSomeTreatmentsWithCategoryTest {

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass when treatments with correct category meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should return undetermined when trial treatments meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function = HasHadSomeTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 2)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadSomeTreatmentsWithCategory(MATCHING_CATEGORY, 2)
    }
}