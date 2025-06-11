package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat


import org.junit.Test

class HasHadProgressionFollowingLatestTreatmentLineTest {

    private val function = HasHadProgressionFollowingLatestTreatmentLine()
    private val functionCannotAssumePDIfStopYearProvided = HasHadProgressionFollowingLatestTreatmentLine(canAssumePDIfStopYearProvided = false)
    private val functionPDMustBeRadiological = HasHadProgressionFollowingLatestTreatmentLine(mustBeRadiological = false)

    @Test
    fun `Should fail when treatment history empty`() {
        val treatments = TreatmentTestFactory.withTreatmentHistory(emptyList())
        val evaluation = function.evaluate(treatments)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("No systemic treatments found in treatment history.")
    }

    @Test
    fun `Should fail when no systemic treatments`() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", false))))
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("No systemic treatments found in treatment history.")
    }

    @Test
    fun `Should pass when all systemic treatments with PD response`() {
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
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertThat(evaluation.passMessages).containsExactly("All systemic treatments resulted in progressive disease.")
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should pass when last systemic treatment resulted in PD`() {
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
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment resulted in PD (assumed PD is radiological)")
    }

    @Test
    fun `Should fail when last treatment stopped but no PD response`() {
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
        val evaluation = functionCannotAssumePDIfStopYearProvided.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("Last systemic treament did not result in progressive disease.")
    }

    @Test
    fun `Should pass when last treatment stopped but no PD response and radiological pd assumed`() {
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
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment stopped and radiological progression is assumed.")
    }

    @Test
    fun `Should pass with must be radiological` () {
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
        val evaluation = functionPDMustBeRadiological.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Last systemic treatment resulted in PD")
    }

    @Test
    fun `Should be undetermined when last treatment has PD but treatment without date does not`() {
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
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("Unable to determine radiological progression following latest treatment line due to treatments without start date.")
    }

    @Test
    fun `Should be undetermined when treatment without date has PD but last treatment does not`() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("1", true)),
                stopReason = StopReason.TOXICITY,
                bestResponse = TreatmentResponse.STABLE_DISEASE,
                startYear = 2024,
                startMonth = 10
            ),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("2", true)),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                startYear = null,
                startMonth = null
            )
        )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("Unable to determine radiological progression following latest treatment line due to treatments without start date.")
    }
}
