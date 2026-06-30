package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions.treatmentStoppedDueToPD
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
    fun `treatmentStoppedDueToPD should return null when best response is PD but stop reason and duration are null`() {
        assertThat(treatmentStoppedDueToPD(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE))).isNull()
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null))).isTrue()
        }
    }

    @Test
    fun `Should return true when stop reason is null and duration was sufficient`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntryWithDates(null, null, STOP_MONTH_SUFFICIENT_DURATION))).isTrue()
        }
    }

    @Test
    fun `Should return null when stop reason is null and duration was insufficient`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntryWithDates(null, null, STOP_MONTH_INSUFFICIENT_DURATION))).isNull()
        }
    }

    @Test
    fun `Should return null when stop reason is null and best response is not PD`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntry(null, TreatmentResponse.MIXED))).isNull()
        }
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is null`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntry(StopReason.TOXICITY, null))).isFalse()
        }
    }

    @Test
    fun `Should return true when stop reason is PD and best response is not PD`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED))).isTrue()
        }
    }

    @Test
    fun `Should return true when stop reason is not PD and best response is PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE))).isTrue()
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is PD`() {
        assertThat(treatmentStoppedDueToPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE))).isFalse()
    }

    @Test
    fun `Should return false when stop reason is not PD`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(function(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED))).isFalse()
        }
    }

    @Test
    fun `Should return false when stop reason is not PD also if treatment duration was sufficient`() {
        listOf<(TreatmentHistoryEntry) -> Boolean?>({ treatmentResultedInPD(it) }, { treatmentStoppedDueToPD(it) }).forEach { function ->
            assertThat(
                function(
                    treatmentHistoryEntryWithDates(
                        StopReason.TOXICITY,
                        TreatmentResponse.MIXED,
                        STOP_MONTH_SUFFICIENT_DURATION
                    )
                )
            ).isFalse()
        }
    }

    @Test
    fun `Should return true when stop reason is null and subsequent line starts within 26 weeks`() {
        val entry = treatmentHistoryEntryWithDates(null, null, STOP_MONTH_INSUFFICIENT_DURATION)
        val subsequentEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("next treatment", true)),
            startYear = STOP_YEAR, startMonth = STOP_MONTH_INSUFFICIENT_DURATION + 2
        )
        val history = listOf(entry, subsequentEntry)
        assertThat(treatmentResultedInPD(entry, history)).isTrue()
        assertThat(treatmentStoppedDueToPD(entry, history)).isTrue()
    }

    @Test
    fun `Should not infer PD from subsequent line when stop reason is toxicity`() {
        val entry = treatmentHistoryEntryWithDates(StopReason.TOXICITY, null, STOP_MONTH_INSUFFICIENT_DURATION)
        val subsequentEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("next treatment", true)),
            startYear = STOP_YEAR, startMonth = STOP_MONTH_INSUFFICIENT_DURATION + 2
        )
        val history = listOf(entry, subsequentEntry)
        assertThat(treatmentResultedInPD(entry, history)).isFalse()
        assertThat(treatmentStoppedDueToPD(entry, history)).isFalse()
    }

    @Test
    fun `Should return null when stop reason is null and subsequent line starts more than 26 weeks later`() {
        val entry = treatmentHistoryEntryWithDates(null, null, STOP_MONTH_INSUFFICIENT_DURATION)
        val distantEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("next treatment", true)),
            startYear = STOP_YEAR + 1, startMonth = 1
        )
        val history = listOf(entry, distantEntry)
        assertThat(treatmentResultedInPD(entry, history)).isNull()
        assertThat(treatmentStoppedDueToPD(entry, history)).isNull()
    }

    @Test
    fun `Should return null when treatment has no stop date`() {
        val entry = treatmentHistoryEntry(null, null)
        val subsequentEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("next treatment", true)),
            startYear = STOP_YEAR, startMonth = STOP_MONTH_INSUFFICIENT_DURATION + 2
        )
        assertThat(treatmentResultedInPD(entry, listOf(entry, subsequentEntry))).isNull()
        assertThat(treatmentStoppedDueToPD(entry, listOf(entry, subsequentEntry))).isNull()
    }

    @Test
    fun `Should return true when another treatment started during and outlasted this treatment`() {
        val entry = treatmentHistoryEntryWithDates(null, null, STOP_MONTH_SUFFICIENT_DURATION)
        val overlappingEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("overlapping treatment", true)),
            startYear = START_YEAR, startMonth = STOP_MONTH_INSUFFICIENT_DURATION,
            stopYear = STOP_YEAR + 1, stopMonth = 1
        )
        val history = listOf(entry, overlappingEntry)
        assertThat(treatmentResultedInPD(entry, history)).isTrue()
        assertThat(treatmentStoppedDueToPD(entry, history)).isTrue()
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