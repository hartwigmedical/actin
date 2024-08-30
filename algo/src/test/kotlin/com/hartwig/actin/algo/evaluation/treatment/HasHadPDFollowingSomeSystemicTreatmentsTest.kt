package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasHadPDFollowingSomeSystemicTreatmentsTest {

    @Test
    fun shouldFailWhenNoTreatments() {
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        }
    }

    @Test
    fun shouldFailWhenOnlyNonSystemicTreatment() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", false))))

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun shouldBeUndeterminedWhenLastSystemicTreatmentHasNoEndDate() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020))

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun shouldPassWithOneSystemicTreatmentWithPDStopReason() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020, stopReason = StopReason.PROGRESSIVE_DISEASE))

        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))

        val radiologicalEvaluation = RADIOLOGICAL_FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, radiologicalEvaluation)
        assertThat(radiologicalEvaluation.passGeneralMessages).hasSize(1)
        assertThat(radiologicalEvaluation.passGeneralMessages.iterator().next()).contains("(assumed PD is radiological)")
    }

    @Test
    fun shouldBeUndeterminedWhenLaterSystemicTreatmentHasStopReasonToxicity() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("1", true)), 2020, stopReason = StopReason.PROGRESSIVE_DISEASE),
            treatmentHistoryEntry(setOf(treatment("1", true)), 2021, stopReason = StopReason.TOXICITY)
        )

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun shouldPassWhenLastSystemicTreatmentHasShortEndDateAndOtherOrUnknownStopReason() {
        val treatments = listOf(
            treatmentHistoryEntry(
                setOf(treatment("1", true)),
                startYear = 2020,
                stopYear = 2020
            )
        )

        FUNCTIONS.forEach {
            val evaluation = it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            assertEvaluation(EvaluationResult.PASS, evaluation)
            assertThat(evaluation.passGeneralMessages).hasSize(1)
            assertThat(evaluation.passGeneralMessages.iterator().next()).contains("PD is assumed")
        }
    }

    @Test
    fun shouldPassWhenLastSystemicTreatmentHasLateEndDateAndOtherOrUnknownStopReason() {
        val treatments = listOf(
            treatmentHistoryEntry(
                setOf(treatment("1", true)),
                startYear = 2020,
                stopYear = 2022
            )
        )

        FUNCTIONS.forEach {
            val evaluation = it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            assertEvaluation(EvaluationResult.PASS, evaluation)
            assertThat(evaluation.passGeneralMessages).hasSize(1)
            assertThat(evaluation.passGeneralMessages.iterator().next()).contains("with PD")
        }
    }

    @Test
    fun shouldPassIfLastSystemicTreatmentIndicatesPDInBestResponse() {
        val treatments =
            listOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE))
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun shouldReturnUndeterminedWhenProvidedWithMultipleUninterruptedTreatmentsToReachMinimum() {
        val function = HasHadPDFollowingSomeSystemicTreatments(2, false)

        val treatmentSet = setOf(treatment("1", true))
        val treatments = listOf(
            treatmentHistoryEntry(treatmentSet, 2020),
            treatmentHistoryEntry(treatmentSet, 2021)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    companion object {
        private val FUNCTION = HasHadPDFollowingSomeSystemicTreatments(1, false)
        private val RADIOLOGICAL_FUNCTION = HasHadPDFollowingSomeSystemicTreatments(1, true)
        private val FUNCTIONS = listOf(FUNCTION, RADIOLOGICAL_FUNCTION)
    }
}