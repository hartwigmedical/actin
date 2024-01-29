package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

class HasPreviouslyParticipatedInTrialTest {

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailWithNonTrialTreatments() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(treatment("some treatment", true)), isTrial = false)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldPassWithTrialTreatments() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(treatment("some treatment", true)), isTrial = true)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    companion object {
        val FUNCTION = HasPreviouslyParticipatedInTrial()
    }
}