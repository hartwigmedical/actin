package com.hartwig.actin.algo.evaluation.treatment

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
}