package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat


import org.junit.Test

class HasRadiologicalProgressionFollowingLatestTreatmentLineTest {

    @Test
    fun shouldFailWhenTreatmentHistoryEmpty() {
        val treatments = TreatmentTestFactory.withTreatmentHistory(emptyList())
        val evaluation = FUNCTION.evaluate(treatments)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("No systemic treatments found in treatment history.")
    }

    @Test
    fun shouldFailWhenOnlyNonSystemicTreatments() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", false))))
        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("No systemic treatments found in treatment history.")
    }

    @Test
    fun shouldPassWhenAllSystemicTreatmentResultedInPD() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                stopYear = null,
                stopMonth = null
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                stopYear = null,
                stopMonth = null
            )
        )
        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertThat(evaluation.passMessages).containsExactly("All systemic treatments resulted in progressive disease.")
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun shouldPassWhenLastSystemicTreatmentResultedInPD() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                startYear = 2024,
                startMonth = 10,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                stopYear = 2025,
                stopMonth = 9
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                startYear = 2025,
                startMonth = 10,
                stopYear = 2026,
                stopMonth = null
            )
        )
        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment resulted in PD (assumed PD is radiological)")
    }

    @Test
    fun shouldFailLastTreatmentDidNotResultInPD() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                startYear = 2024,
                startMonth = 10,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                stopYear = 2025,
                stopMonth = 9
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.TOXICITY,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                startYear = 2025,
                startMonth = 10,
                stopYear = 2026,
                stopMonth = null
            )
        )
        val evaluation = FUNCTION_CANNOT_ASSUME_PD_IF_STOP_YEAR_PROVIDED.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("Last systemic treament did not result in progressive disease.")
    }

    @Test
    fun shouldPassWhenLastTreatmentStoppedAndRadiologicalProgressionAssumed() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                startYear = 2024,
                startMonth = 10,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                stopYear = 2025,
                stopMonth = 9
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                startYear = 2025,
                startMonth = 10,
                stopYear = 2025,
                stopMonth = 11
            )
        )
        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment stopped and radiological progression is assumed.")
    }

    @Test
    fun shouldPassWithMustBeRadiological() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                startYear = 2024,
                startMonth = 10,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                stopYear = 2025,
                stopMonth = 9
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                startYear = 2025,
                startMonth = 10,
                stopYear = 2026,
                stopMonth = null
            )
        )
        val evaluation = FUNCTION_PD_MUST_BE_RADIOLOGICAL.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment resulted in PD")
    }

    @Test
    fun unableToDetermineRadiologicalProgressionWhenTreatmentsWithoutStartDate() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                startYear = null,
                startMonth = null
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                startYear = 2025,
                startMonth = null
            )
        )
        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("Unable to determine radiological progression following latest treatment line due to treatments without start date.")
    }


    companion object {
        private val FUNCTION = HasRadiologicalProgressionFollowingLatestTreatmentLine()
        private val FUNCTION_CANNOT_ASSUME_PD_IF_STOP_YEAR_PROVIDED = HasRadiologicalProgressionFollowingLatestTreatmentLine(canAssumePDIfStopYearProvided = false)
        private val FUNCTION_PD_MUST_BE_RADIOLOGICAL = HasRadiologicalProgressionFollowingLatestTreatmentLine(mustBeRadiological = false)
    }
}
