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
    fun shouldReturnTrueWhenStopReasonIsNullAndBestResponseIsPD() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE)))
    }

    @Test
    fun shouldReturnTrueWhenStopReasonIsPDAndBestResponseIsNull() {
        assertEquals(true, treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null)))
    }

    @Test
    fun shouldBeNullWhenStopReasonIsNullAndBestResponseIsNotPD() {
        assertNull(treatmentResultedInPDOption(treatmentHistoryEntry(null, TreatmentResponse.MIXED)))
    }

    @Test
    fun shouldBeNullWhenStopReasonIsNotPDAndBestResponseIsNull() {
        assertNull(treatmentResultedInPDOption(treatmentHistoryEntry(StopReason.TOXICITY, null)))
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
    }
}