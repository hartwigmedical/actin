package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressiveDiseaseFunctionsTest {
    val startMonth = 1
    val startYear = 1999
    val stopMonthSufficientDuration = 9
    val stopYearSufficientDuration = 1999
    val stopMonthInsufficientDuration = 4
    val stopYearInsufficientDuration = 1999

    @Test
    fun `Should return true when stop reason is null and best response is PD and duration null`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null)))
    }

    @Test
    fun `Should return true when stop reason is null and duration was sufficient`() {
        assertEquals(
            true,
            treatmentResultedInPDOption(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    startYear,
                    startMonth,
                    stopYearSufficientDuration,
                    stopMonthSufficientDuration
                )
            )
        )
    }

    @Test
    fun `Should return null when stop reason is null and duration was insufficient`() {
        assertNull(
            treatmentResultedInPDOption(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    startYear,
                    startMonth,
                    stopYearInsufficientDuration,
                    stopMonthInsufficientDuration
                )
            )
        )
    }

    @Test
    fun `Should return null when stop reason is null and best response is not PD`() {
        assertNull(treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is null`() {
        assertEquals(false, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, null)))
    }

    @Test
    fun `Should return true when stop reason is PD and best response is not PD`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return true when stop reason is not PD and best response is PD`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun `Should return false when stop reason is not PD`() {
        assertEquals(false, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED)))
    }

    @Test
    fun `Should return false when stop reason is not PD also if treatment duration was sufficient`() {
        assertEquals(
            false,
            treatmentResultedInPDOption(
                treatmentHistoryEntryWithDates(
                    StopReason.TOXICITY,
                    TreatmentResponse.MIXED,
                    startYear,
                    startMonth,
                    stopYearSufficientDuration,
                    stopMonthSufficientDuration
                )
            )
        )
    }

    companion object {
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
            startYear: Int? = null,
            startMonth: Int? = null,
            stopYear: Int?,
            stopMonth: Int?
        ): TreatmentHistoryEntry {
            return TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment("test treatment", true)),
                stopReason = stopReason,
                bestResponse = bestResponse,
                startYear = startYear,
                startMonth = startMonth,
                stopYear = stopYear,
                stopMonth = stopMonth
            )
        }
    }
}