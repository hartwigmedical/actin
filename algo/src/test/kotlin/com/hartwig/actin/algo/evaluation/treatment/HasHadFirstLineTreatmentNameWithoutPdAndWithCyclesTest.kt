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

class HasHadFirstLineTreatmentNameWithoutPdAndWithCyclesTest {

    private val matchingTreatment = treatment("matching", true)
    private val matchingHistoryEntry = treatmentHistoryEntry(
        setOf(matchingTreatment),
        startYear = RECENT_YEAR,
        stopReason = StopReason.TOXICITY,
        numCycles = MIN_CYCLES
    )

    private val nonMatchingHistoryEntry = treatmentHistoryEntry(setOf(treatment("wrong", true)))
    private val function = HasHadFirstLineTreatmentNameWithoutPdAndWithCycles(matchingTreatment.name, MIN_CYCLES)

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient has not received correct treatment`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(nonMatchingHistoryEntry))))
    }

    @Test
    fun `Should fail when correct treatment is not first line`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(listOf(nonMatchingHistoryEntry.copy(startYear = RECENT_YEAR.minus(1)), matchingHistoryEntry))
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
                        matchingHistoryEntry.copy(
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
                    listOf(matchingHistoryEntry.copy(treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES - 1)))
                )
            )
        )
    }

    @Test
    fun `Should pass when correct treatment is first line without PD and with sufficient cycles`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(matchingHistoryEntry))))
    }

    @Test
    fun `Should pass when treatment has unknown dates but is only treatment and meets PD and cycles criteria`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(matchingHistoryEntry.copy(startYear = null))))
        )
    }

    @Test
    fun `Should be undetermined when correct treatment has unknown date and multiple treatments in history`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                withTreatmentHistory(
                    listOf(nonMatchingHistoryEntry.copy(startYear = RECENT_YEAR), matchingHistoryEntry.copy(startYear = null))
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
                    listOf(matchingHistoryEntry.copy(treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = null)))
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
                    listOf(matchingHistoryEntry.copy(treatmentHistoryDetails = TreatmentHistoryDetails(cycles = null)))
                )
            )
        )
    }
}