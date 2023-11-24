package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressiveDiseaseFunctionsTest {
    @Test
    fun `Should return true when stop reason is null and best response is PD and duration null`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null)))
    }

    @Test
    fun `Should return true when stop reason is null and duration at least 26 weeks`() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntryWithDates(null, null, 1999, 1, 2000, 1)))
    }

    @Test
    fun shouldBeNullWhenStopReasonIsNullAndBestResponseIsNotPD() {
        assertNull(treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.MIXED)))
    }

    @Test
    fun shouldBeNullWhenStopReasonIsNotPDAndBestResponseIsNull() {
        assertEquals(false, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, null)))
    }

    @Test
    fun shouldReturnTrueWhenStopReasonIsPDAndBestResponseIsNotPD() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED)))
    }

    @Test
    fun shouldReturnTrueWhenStopReasonIsNotPDAndBestResponseIsPD() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun shouldReturnFalseWhenStopReasonAndBestResponseAreKnownAndNotPD() {
        assertEquals(false, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED)))
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