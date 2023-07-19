package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.drugTherapy
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSomeTreatmentsWithCategoryTest {

    @Test
    fun shouldFailForNoTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForWrongTreatmentCategory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldPassWhenTreatmentsWithCorrectCategoryMeetThreshold() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenTrialTreatmentsMeetThreshold() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldIgnoreTrialMatchesAndFailWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadSomeTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 2)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadSomeTreatmentsWithCategory(MATCHING_CATEGORY, 2)
    }
}