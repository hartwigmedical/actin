package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasHadPDFollowingSomeSystemicTreatmentsTest {

    @Test
    fun shouldFailWhenNoTreatments() {
        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList()))
            )
        }
    }

    @Test
    fun shouldFailWhenOnlyNonSystemicTreatment() {
        val treatments = listOf(TreatmentTestFactory.builder().isSystemic(false).build())

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
            )
        }
    }

    @Test
    fun shouldBeUndeterminedWhenLastSystemicTreatmentHasNoEndDate() {
        val treatments = listOf(
            TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2020)
                .build()
        )

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
            )
        }
    }

    @Test
    fun shouldPassWithOneSystemicTreatmentWithPDStopReason() {
        val treatments = listOf(
            TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2020)
                .stopReason(PD_LABEL)
                .build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )

        val radiologicalEvaluation =
            RADIOLOGICAL_FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        assertEvaluation(EvaluationResult.PASS, radiologicalEvaluation)
        assertThat(radiologicalEvaluation.passGeneralMessages()).hasSize(1)
        assertThat(
            radiologicalEvaluation.passGeneralMessages().iterator().next()
        ).contains("(assumed PD is radiological)")
    }

    @Test
    fun shouldBeUndeterminedWhenLaterSystemicTreatmentHasStopReasonToxicity() {
        val treatments = listOf(
            TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2020)
                .stopReason(PD_LABEL)
                .build(),
            TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2021)
                .stopReason("toxicity")
                .bestResponse("improved")
                .build()
        )

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
            )
        }
    }

    @Test
    fun shouldPassWhenLastSystemicTreatmentHasEndDateAndOtherOrUnknownStopReason() {
        val treatments = listOf(
            TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2020)
                .stopYear(2022)
                .build()
        )

        FUNCTIONS.forEach {
            val evaluation = it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
            assertEvaluation(EvaluationResult.PASS, evaluation)
            assertThat(evaluation.passGeneralMessages()).hasSize(1)
            assertThat(evaluation.passGeneralMessages().iterator().next()).contains("PD is assumed")
        }
    }

    @Test
    fun shouldPassIfLastSystemicTreatmentIndicatesPDInBestResponse() {
        val treatments = listOf(
            TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2021).bestResponse(PD_LABEL)
                .build()
        )
        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.PASS,
                it.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
            )
        }
    }

    @Test
    fun shouldReturnUndeterminedWhenProvidedWithMultipleUninterruptedTreatmentsToReachMinimum() {
        val function = HasHadPDFollowingSomeSystemicTreatments(2, false)

        val treatments = listOf(
            TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2020).build(),
            TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2021).build()
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )
    }

    companion object {
        private val FUNCTION = HasHadPDFollowingSomeSystemicTreatments(1, false)
        private val RADIOLOGICAL_FUNCTION = HasHadPDFollowingSomeSystemicTreatments(1, true)
        private val FUNCTIONS = listOf(FUNCTION, RADIOLOGICAL_FUNCTION)
    }
}