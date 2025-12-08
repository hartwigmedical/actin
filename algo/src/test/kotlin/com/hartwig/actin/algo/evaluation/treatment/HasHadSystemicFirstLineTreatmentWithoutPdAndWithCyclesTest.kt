package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import org.junit.Test

private const val MIN_CYCLES = 3
private const val RECENT_YEAR = 2024
private val MATCHING_TREATMENT = treatment("matching", true)
private val MATCHING_HISTORY_ENTRY = treatmentHistoryEntry(
    setOf(MATCHING_TREATMENT),
    startYear = RECENT_YEAR,
    stopReason = StopReason.TOXICITY,
    numCycles = MIN_CYCLES
)
private val MATCHING_HISTORY_DETAILS = TreatmentHistoryDetails(stopReason = StopReason.TOXICITY, cycles = MIN_CYCLES)

private val NON_MATCHING_HISTORY_ENTRY = treatmentHistoryEntry(setOf(treatment("wrong", true)))

class HasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesTest {

    private val function = HasHadSystemicFirstLineTreatmentWithoutPdAndWithCycles(MATCHING_TREATMENT, MIN_CYCLES)

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient has not received correct treatment`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(NON_MATCHING_HISTORY_ENTRY))))
    }

    @Test
    fun `Should fail when correct treatment is not first line`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(listOf(NON_MATCHING_HISTORY_ENTRY.copy(startYear = RECENT_YEAR.minus(1)), MATCHING_HISTORY_ENTRY))
            )
        )
    }

    @Test
    fun `Should fail when correct treatment is first line but resulted in PD`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        MATCHING_HISTORY_ENTRY.copy(
                            treatmentHistoryDetails = TreatmentHistoryDetails(
                                stopReason = StopReason.PROGRESSIVE_DISEASE
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when correct treatment is first line but with insufficient cycles`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(
                    listOf(MATCHING_HISTORY_ENTRY.copy(treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES - 1)))
                )
            )
        )
    }

    @Test
    fun `Should pass when correct treatment is first line without PD and with sufficient cycles`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(MATCHING_HISTORY_ENTRY))))
    }

    @Test
    fun `Should pass when treatment has unknown dates but is only treatment and meets PD and cycles criteria`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        MATCHING_HISTORY_ENTRY.copy(startYear = null, treatmentHistoryDetails = MATCHING_HISTORY_DETAILS)
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when correct treatment has unknown date and multiple treatments in history`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        NON_MATCHING_HISTORY_ENTRY.copy(startYear = RECENT_YEAR),
                        MATCHING_HISTORY_ENTRY.copy(startYear = null, treatmentHistoryDetails = MATCHING_HISTORY_DETAILS)
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when correct treatment has unknown PD status and other criteria are met`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        MATCHING_HISTORY_ENTRY.copy(
                            treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = null, cycles = MIN_CYCLES)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when correct treatment has unknown number of cycles and other criteria are met`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        MATCHING_HISTORY_ENTRY.copy(
                            treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = StopReason.TOXICITY, cycles = null)
                        )
                    )
                )
            )
        )
    }
}