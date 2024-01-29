package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val START_YEAR = 1999
private const val START_MONTH = 1
private const val STOP_YEAR = 1999
private const val STOP_MONTH_SUFFICIENT_DURATION = 9
private const val STOP_MONTH_INSUFFICIENT_DURATION = 4

class ProgressiveDiseaseFunctionsTest {

    @Test
    fun `Should return true when stop reason is null and best response is PD and duration null`() {
        assertEquals(true, treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        assertEquals(true, treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null)))
    }

    @Test
    fun `Should return true when stop reason is null and duration was sufficient`() {
        assertEquals(
            true,
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    STOP_MONTH_SUFFICIENT_DURATION
                )
            )
        )
    }

    @Test
    fun `Should return null when stop reason is null and duration was insufficient`() {
        assertNull(
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    STOP_MONTH_INSUFFICIENT_DURATION
                )
            )
        )
    }

    @Test
    fun `Should return null when stop reason is null and best response is not PD`() {
        assertNull(treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is null`() {
        assertEquals(false, treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, null)))
    }

    @Test
    fun `Should return true when stop reason is PD and best response is not PD`() {
        assertEquals(true, treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return true when stop reason is not PD and best response is PD`() {
        assertEquals(true, treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun `Should return false when stop reason is not PD`() {
        assertEquals(false, treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return false when stop reason is not PD also if treatment duration was sufficient`() {
        assertEquals(
            false,
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    StopReason.TOXICITY,
                    TreatmentResponse.MIXED,
                    STOP_MONTH_SUFFICIENT_DURATION
                )
            )
        )
    }

    private fun treatmentHistoryEntry(stopReason: StopReason?, bestResponse: TreatmentResponse?): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("test treatment", true)),
            stopReason = stopReason,
            bestResponse = bestResponse
        )
    }

    private fun treatmentHistoryEntryWithDates(
        stopReason: StopReason?,
        bestResponse: TreatmentResponse?,
        stopMonth: Int?
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("test treatment", true)),
            stopReason = stopReason,
            bestResponse = bestResponse,
            startYear = START_YEAR,
            startMonth = START_MONTH,
            stopYear = STOP_YEAR,
            stopMonth = stopMonth
        )
    }
}