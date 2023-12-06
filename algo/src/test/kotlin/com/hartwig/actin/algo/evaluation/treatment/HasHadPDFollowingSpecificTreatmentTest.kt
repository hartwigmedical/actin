package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadPDFollowingSpecificTreatmentTest {

    @Test
    fun shouldFailForEmptyTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForOtherTreatmentWithPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(NON_MATCHING_TREATMENTS, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForMatchingTreatmentButNoPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENTS,
            stopReason = StopReason.TOXICITY,
            bestResponse = TreatmentResponse.MIXED
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForMatchingTreatmentMissingStopReason() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingTreatmentAndStopReasonPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingSwitchToTreatmentAndStopReasonPD() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            NON_MATCHING_TREATMENTS,
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            switchToTreatments = listOf(treatmentStage(MATCHING_TREATMENTS.first()))
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedWithTrialTreatmentEntryInHistory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_TREATMENTS = listOf(treatment("treatment", true))
        private val NON_MATCHING_TREATMENTS = setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY))
        private val FUNCTION = HasHadPDFollowingSpecificTreatment(MATCHING_TREATMENTS)
    }
}