package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.lastSystemicTreatment
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.maxSystemicTreatments
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.minSystemicTreatments
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertNull
import org.junit.Test

class SystemicTreatmentAnalyserTest {
    @Test
    fun shouldReturnZeroWhenTreatmentListEmpty() {
        val treatmentHistory = emptyList<TreatmentHistoryEntry>()
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
    }

    @Test
    fun shouldReturnOneWhenOneSystemicTreatmentProvided() {
        val treatmentHistory = listOf(SYSTEMIC_TREATMENT_HISTORY_ENTRY)
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(1)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(1)
    }

    @Test
    fun shouldNotCountNonSystemicTreatments() {
        val treatmentHistory = listOf(NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY)
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
    }

    @Test
    fun shouldCountBlocksOfConsecutiveSystemicTreatmentsForMinAndEachSystemicTreatmentForMax() {
        val treatmentHistory = mutableListOf(
            SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            EARLIER_SYSTEMIC_TREATMENT_HISTORY_ENTRY
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)

        treatmentHistory.add(treatmentHistoryEntry(setOf(SYSTEMIC_TREATMENT), 2021, 10))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)
    }

    @Test
    fun shouldNotCountInterruptionsBetweenTreatmentsWithSameNameAndUnknownDates() {
        val treatmentHistory = mutableListOf(
            SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            EARLIER_SYSTEMIC_TREATMENT_HISTORY_ENTRY
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)

        treatmentHistory.add(treatmentHistoryEntry(setOf(SYSTEMIC_TREATMENT), null, null))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)

        treatmentHistory.add(treatmentHistoryEntry(setOf(SYSTEMIC_TREATMENT), 2021, null))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(4)
    }

    @Test
    fun shouldCountInterruptionsBetweenDifferentTreatmentsAndUnknownDates() {
        val treatmentHistory = mutableListOf(
            SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY,
            EARLIER_SYSTEMIC_TREATMENT_HISTORY_ENTRY
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)

        treatmentHistory.add(
            treatmentHistoryEntry(
                setOf(treatment("treatment C", true)), null, null
            )
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)
    }

    @Test
    fun shouldReturnNullForLastSystemicTreatmentWhenNoTreatmentsProvided() {
        assertThat(lastSystemicTreatment(emptyList())).isNull()
    }

    @Test
    fun shouldReturnNullForLastSystemicTreatmentWhenOnlyNonSystemicTreatments() {
        assertNull(lastSystemicTreatment(listOf(NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY)))
    }

    @Test
    fun canDetermineLastSystemicTreatment() {
        val treatmentHistory = mutableListOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020, 5))
        assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory, "1")

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("2", true)), 2021))
        assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory, "2")

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("3", true)), 2021, 1))
        assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory, "3")

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("4", true)), 2021, 10))
        assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory, "4")

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("5", true)), 2021, 8))
        assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory, "4")
    }

    companion object {
        private val SYSTEMIC_TREATMENT = treatment("treatment A", true)
        private val SYSTEMIC_TREATMENT_HISTORY_ENTRY = treatmentHistoryEntry(setOf(SYSTEMIC_TREATMENT), 2022, 5)
        private val EARLIER_SYSTEMIC_TREATMENT_HISTORY_ENTRY = SYSTEMIC_TREATMENT_HISTORY_ENTRY.copy(startYear = 2021, startMonth = 5)
        private val NON_SYSTEMIC_TREATMENT = treatment("treatment B", false)
        private val NON_SYSTEMIC_TREATMENT_HISTORY_ENTRY = treatmentHistoryEntry(setOf(NON_SYSTEMIC_TREATMENT), 2022, 2)

        private fun assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory: List<TreatmentHistoryEntry>, name: String) {
            assertThat(lastSystemicTreatment(treatmentHistory)!!.treatments.first().name).isEqualTo(name)
        }
    }
}