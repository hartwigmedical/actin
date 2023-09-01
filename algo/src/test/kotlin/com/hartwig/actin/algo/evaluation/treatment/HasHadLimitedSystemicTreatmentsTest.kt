package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import org.junit.Test

class HasHadLimitedSystemicTreatmentsTest {

    @Test
    fun shouldPassWhenTreatmentHistoryEmpty() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassWhenOnlyNonSystemicTreatments() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", false))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldPassWhenSystemicTreatmentsBelowThreshold() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", true))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldFailWhenSystemicTreatmentsEqualThreshold() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("1", true))),
            treatmentHistoryEntry(setOf(treatment("2", true)))
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldBeUndeterminedInCaseOfAmbiguousTimeline() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("treatment", true)))
        val treatments = listOf(treatmentHistoryEntry, treatmentHistoryEntry)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    companion object {
        private val FUNCTION = HasHadLimitedSystemicTreatments(1)
    }
}