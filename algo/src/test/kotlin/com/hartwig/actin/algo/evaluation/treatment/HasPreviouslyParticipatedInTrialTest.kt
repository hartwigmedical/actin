package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

const val ACRONYM = "ACR"
val FUNCTION_WITH_ACRONYM = HasPreviouslyParticipatedInTrial(ACRONYM)

class HasPreviouslyParticipatedInTrialTest {

    @Test
    fun `Should fail with empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass with trial treatment matching acronym`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(isTrial = true, trialAcronym = ACRONYM.lowercase()))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should be undetermined with no match but entry having null acronym`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(isTrial = true, trialAcronym = "OTHER"),
            treatmentHistoryEntry(isTrial = true)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail with trial treatments that do not match acronym`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(isTrial = true, trialAcronym = "OTHER"),
            treatmentHistoryEntry(isTrial = true, trialAcronym = "ANOTHER")
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(treatmentHistory)))
    }
}