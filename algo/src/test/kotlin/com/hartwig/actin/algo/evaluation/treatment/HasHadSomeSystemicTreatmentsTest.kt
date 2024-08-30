package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.Test

class HasHadSomeSystemicTreatmentsTest {

    @Test
    fun shouldFailWhenTreatmentHistoryEmpty() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailWhenOnlyNonSystemicTreatments() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", false))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldPassWhenSystemicTreatmentsEqualThreshold() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldPassWhenSystemicTreatmentsExceedThreshold() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true))),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)))
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldBeUndeterminedInCaseOfAmbiguousTimeline() {
        val function = HasHadSomeSystemicTreatments(2)
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("treatment", true)))
        val treatments = listOf(treatmentHistoryEntry, treatmentHistoryEntry)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    companion object {
        private val FUNCTION = HasHadSomeSystemicTreatments(1)
    }
}