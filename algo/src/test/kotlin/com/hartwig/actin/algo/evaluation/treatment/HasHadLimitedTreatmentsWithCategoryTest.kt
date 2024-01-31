package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryTest {

    @Test
    fun shouldPassForNoTreatments() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassForWrongTreatmentCategory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldFailWhenTreatmentsWithCorrectCategoryExceedLimit() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenTrialTreatmentsExceedLimit() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldIgnoreTrialMatchesAndPassWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadLimitedTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 1)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadLimitedTreatmentsWithCategory(MATCHING_CATEGORY, 1)
    }
}