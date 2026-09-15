package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import org.junit.jupiter.api.Test

class HasHadLimitedSystemicTreatmentsTest {
    
    private val function = HasHadLimitedSystemicTreatments(1)

    @Test
    fun shouldPassWhenTreatmentHistoryEmpty() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())),
            "At most 1 systemic treatments in provided treatments"
        )
    }

    @Test
    fun shouldPassWhenOnlyNonSystemicTreatments() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", false))))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)),
            "At most 1 systemic treatments in provided treatments"
        )
    }

    @Test
    fun shouldPassWhenSystemicTreatmentsBelowThreshold() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", true))))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)),
            "At most 1 systemic treatments in provided treatments"
        )
    }

    @Test
    fun shouldFailWhenSystemicTreatmentsEqualThreshold() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("1", true))),
            treatmentHistoryEntry(setOf(treatment("2", true)))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)),
            "More than 1 systemic treatments in provided treatments"
        )
    }

    @Test
    fun shouldBeUndeterminedInCaseOfAmbiguousTimeline() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("treatment", true)))
        val treatments = listOf(treatmentHistoryEntry, treatmentHistoryEntry)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)),
            "Undetermined if provided treatments include more than 1 systemic treatments"
        )
    }
}