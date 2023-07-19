package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.drugTherapy
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadPDFollowingTreatmentWithCategoryTest {

    @Test
    fun shouldFailForEmptyTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForWrongCategoryWithPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTherapy("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForRightCategoryButNoPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            bestResponse = TreatmentResponse.MIXED
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForRightCategoryAndMissingStopReason() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForRightCategoryAndStopReasonPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedWithTrialTreatmentEntryInHistory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)),
            isTrial = true
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldIgnoreTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadPDFollowingTreatmentWithCategory(TreatmentCategory.TRANSPLANTATION)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TREATMENT_SET = setOf(drugTherapy("test", MATCHING_CATEGORY))
        private val FUNCTION = HasHadPDFollowingTreatmentWithCategory(MATCHING_CATEGORY)
    }
}