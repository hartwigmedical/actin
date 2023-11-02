package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadPDFollowingSpecificTreatmentTest {

    @Test
    fun shouldFailForEmptyTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForOtherTreatmentWithPD() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForMatchingTreatmentButNoPD() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENTS,
            stopReason = StopReason.TOXICITY,
            bestResponse = TreatmentResponse.MIXED
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForMatchingTreatmentMissingStopReason() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENTS)
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun shouldPassForMatchingTreatmentAndStopReasonPD() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENTS, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENTS, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedWithTrialTreatmentEntryInHistory() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)),
            isTrial = true
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    companion object {
        private val MATCHING_TREATMENTS = listOf(TreatmentTestFactory.treatment("treatment", true))
        private val FUNCTION = HasHadPDFollowingSpecificTreatment(MATCHING_TREATMENTS)
    }
}