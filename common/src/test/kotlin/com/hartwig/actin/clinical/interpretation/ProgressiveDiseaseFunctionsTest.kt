package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val START_YEAR = 1999
private const val START_MONTH = 1
private const val STOP_YEAR = 1999
private const val STOP_MONTH_SUFFICIENT_DURATION = 9
private const val STOP_MONTH_INSUFFICIENT_DURATION = 4

class ProgressiveDiseaseFunctionsTest {

    @Test
    fun `Should return true when stop reason is null and best response is PD and duration null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is null and duration was sufficient`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntryWithDates(null, null, STOP_MONTH_SUFFICIENT_DURATION))).isTrue()
    }

    @Test
    fun `Should return null when stop reason is null and duration was insufficient`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntryWithDates(null, null, STOP_MONTH_INSUFFICIENT_DURATION))).isNull()
    }

    @Test
    fun `Should return null when stop reason is null and best response is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.MIXED))).isNull()
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, null))).isFalse()
    }

    @Test
    fun `Should return true when stop reason is PD and best response is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is not PD and best response is PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE))).isTrue()
    }

    @Test
    fun `Should return false when stop reason is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED))).isFalse()
    }

    @Test
    fun `Should return false when stop reason is not PD also if treatment duration was sufficient`() {
        assertThat(
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    StopReason.TOXICITY,
                    TreatmentResponse.MIXED,
                    STOP_MONTH_SUFFICIENT_DURATION
                )
            )
        ).isFalse()
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