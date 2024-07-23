package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

const val ACRONYM = "ACR"
val FUNCTION = HasPreviouslyParticipatedInTrial()
val FUNCTION_WITH_ACRONYM = HasPreviouslyParticipatedInTrial(ACRONYM)

class HasPreviouslyParticipatedInTrialTest {

    @Test
    fun `Should fail with empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with non trial treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(isTrial = false))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass with trial treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(isTrial = true))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass with trial treatment matching acronym`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(isTrial = true, trialAcronym = ACRONYM.lowercase()))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail with trial treatments that do not match acronym`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(isTrial = true, trialAcronym = "OTHER"),
            treatmentHistoryEntry(isTrial = true)
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_ACRONYM.evaluate(withTreatmentHistory(treatmentHistory)))
    }
}