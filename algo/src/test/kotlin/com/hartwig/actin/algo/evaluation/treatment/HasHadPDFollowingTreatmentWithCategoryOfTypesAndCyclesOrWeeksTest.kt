package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTest {

    @Test
    fun shouldFailForEmptyTreatments() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForWrongCategoryWithPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForRightCategoryAndTypeButNoPD() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, bestResponse = TreatmentResponse.MIXED)
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForRightCategoryAndMissingTypeWithPD() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForRightCategoryTypeAndMissingStopReason() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForRightCategoryTypeAndStopReasonPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedWithTrialTreatmentEntryInHistory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldIgnoreTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
            null, null
        )
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForRightCategoryTypeAndStopReasonPDWhenCyclesAreRequiredAndMissing() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.UNDETERMINED, function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForRightCategoryTypeAndStopReasonPDWhenCyclesAreRequiredAndInsufficient() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE, numCycles = 4
        )
        assertEvaluation(EvaluationResult.FAIL, function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForRightCategoryTypeAndStopReasonPDWhenCyclesAreRequiredAndSufficient() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE, numCycles = 5
        )
        assertEvaluation(EvaluationResult.PASS, function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForRightCategoryTypeAndStopReasonPDWhenMinWeeksRequiredAndUnknown() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.UNDETERMINED, function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForRightCategoryTypeAndStopReasonPDWhenMinWeeksRequiredAndInsufficient() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 5
        )
        assertEvaluation(EvaluationResult.FAIL, function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForRightCategoryTypeAndStopReasonPDWhenMinWeeksRequiredAndSufficient() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        assertEvaluation(EvaluationResult.PASS, function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MATCHING_TREATMENT_SET = setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))

        private fun function(minCycles: Int? = null, minWeeks: Int? = null): HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks {
            return HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                MATCHING_CATEGORY, MATCHING_TYPE_SET, minCycles, minWeeks
            )
        }
    }
}