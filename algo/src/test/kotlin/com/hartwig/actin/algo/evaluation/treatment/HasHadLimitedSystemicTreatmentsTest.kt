package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import org.junit.Test

class HasHadLimitedSystemicTreatmentsTest {
    private val function = HasHadLimitedSystemicTreatments(1)

    @Test
    fun shouldPassWhenTreatmentHistoryEmpty() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassWhenOnlyNonSystemicTreatments() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", false))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldPassWhenSystemicTreatmentsBelowThreshold() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", true))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldFailWhenSystemicTreatmentsEqualThreshold() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("1", true))),
            treatmentHistoryEntry(setOf(treatment("2", true)))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldBeUndeterminedInCaseOfAmbiguousTimeline() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("treatment", true)))
        val treatments = listOf(treatmentHistoryEntry, treatmentHistoryEntry)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }
}