package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.lastSystemicTreatment
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.maxSystemicTreatments
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.minSystemicTreatments
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SystemicTreatmentAnalyserTest {

    private val systemicTreatment = treatment("treatment A", isSystemic = true)
    private val systemicTreatmentHistoryEntry = treatmentHistoryEntry(setOf(systemicTreatment), 2022, 5)
    private val earlierSystemicTreatmentHistoryEntry = systemicTreatmentHistoryEntry.copy(startYear = 2021, startMonth = 5)
    private val nonSystemicTreatment = treatment("treatment B", isSystemic = false)
    private val nonSystemicTreatmentHistoryEntry = treatmentHistoryEntry(setOf(nonSystemicTreatment), 2022, 2)

    @Test
    fun `Should return zero when treatment list is empty`() {
        val treatmentHistory = emptyList<TreatmentHistoryEntry>()
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
    }

    @Test
    fun `Should return one when one systemic treatment provided`() {
        val treatmentHistory = listOf(systemicTreatmentHistoryEntry)
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(1)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(1)
    }

    @Test
    fun `Should not count not systemic treatments`() {
        val treatmentHistory = listOf(nonSystemicTreatmentHistoryEntry)
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(0)
    }

    @Test
    fun `Should count block of consecutive systemic treatments for min and each systemic treatment for max`() {
        val treatmentHistory = mutableListOf(
            systemicTreatmentHistoryEntry,
            nonSystemicTreatmentHistoryEntry,
            earlierSystemicTreatmentHistoryEntry
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)

        treatmentHistory.add(treatmentHistoryEntry(setOf(systemicTreatment), 2021, 10))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)
    }

    @Test
    fun `Should not count interruptions between treatments with same name and unknown dates`() {
        val treatmentHistory = mutableListOf(
            systemicTreatmentHistoryEntry,
            nonSystemicTreatmentHistoryEntry,
            earlierSystemicTreatmentHistoryEntry
        )
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)

        treatmentHistory.add(treatmentHistoryEntry(setOf(systemicTreatment), null, null))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(3)

        treatmentHistory.add(treatmentHistoryEntry(setOf(systemicTreatment), 2021, null))
        assertThat(minSystemicTreatments(treatmentHistory).toLong()).isEqualTo(2)
        assertThat(maxSystemicTreatments(treatmentHistory).toLong()).isEqualTo(4)
    }

    @Test
    fun `Should count interruptions between different treatments and unknown dates`() {
        val treatmentHistory = mutableListOf(
            systemicTreatmentHistoryEntry,
            nonSystemicTreatmentHistoryEntry,
            earlierSystemicTreatmentHistoryEntry
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
    fun `Should return null for last systemic treatment when no treatments provided`() {
        assertThat(lastSystemicTreatment(emptyList())).isNull()
    }

    @Test
    fun `Should return null for last systemic treatment when only non systemic treatments`() {
        assertThat(lastSystemicTreatment(listOf(nonSystemicTreatmentHistoryEntry))).isNull()
    }

    @Test
    fun `Should determine last systemic treatment`() {
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

    private fun assertNameForLastSystemicTreatmentHistoryEntry(treatmentHistory: List<TreatmentHistoryEntry>, name: String) {
        assertThat(lastSystemicTreatment(treatmentHistory)!!.treatments.first().name).isEqualTo(name)
    }
}