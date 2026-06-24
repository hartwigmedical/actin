package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.evaluateTreatmentTimingRelativeToNextLine
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.firstSystemicTreatment
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.lastSystemicTreatment
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.maxSystemicTreatments
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.minSystemicTreatments
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.partitionByIntent
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.partitionRecentTreatments
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SystemicTreatmentAnalyserTest {

    private val systemicTreatment = treatment("treatment A", isSystemic = true)
    private val systemicTreatmentHistoryEntry = treatmentHistoryEntry(setOf(systemicTreatment), 2022, 5)
    private val earlierSystemicTreatmentHistoryEntry = systemicTreatmentHistoryEntry.copy(startYear = 2021, startMonth = 5)
    private val nonSystemicTreatment = treatment("treatment B", isSystemic = false)
    private val nonSystemicTreatmentHistoryEntry = treatmentHistoryEntry(setOf(nonSystemicTreatment), 2022, 2)
    private val referenceDate = LocalDate.of(2025, 12, 1)

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
    fun `Should return null for first and last systemic treatment when no treatments provided`() {
        assertThat(lastSystemicTreatment(emptyList())).isNull()
        assertThat(firstSystemicTreatment(emptyList())).isNull()
    }

    @Test
    fun `Should return null for first and last systemic treatment when only non systemic treatments`() {
        assertThat(lastSystemicTreatment(listOf(nonSystemicTreatmentHistoryEntry))).isNull()
        assertThat(firstSystemicTreatment(listOf(nonSystemicTreatmentHistoryEntry))).isNull()
    }

    @Test
    fun `Should determine last systemic treatment`() {
        val treatmentHistory = mutableListOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020, 5))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "1", ::lastSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("2", true)), 2021))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "2", ::lastSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("3", true)), 2021, 1))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "3", ::lastSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("4", true)), 2021, 10))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "4", ::lastSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("5", true)), 2021, 8))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "4", ::lastSystemicTreatment)
    }

    @Test
    fun `Should determine first systemic treatment`() {
        val treatmentHistory = mutableListOf(treatmentHistoryEntry(setOf(treatment("1", true)), 2020, 5))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "1", ::firstSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("2", true)), 2020))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "2", ::firstSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("3", true)), 2019, 12))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "3", ::firstSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("4", true)), 2019, 11))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "4", ::firstSystemicTreatment)

        treatmentHistory.add(treatmentHistoryEntry(setOf(treatment("5", true)), 2020, 1))
        assertNameForSystemicTreatmentHistoryEntry(treatmentHistory, "4", ::firstSystemicTreatment)
    }

    @Test
    fun `Should determine treatment timing relative to next line`() {
        val history = listOf(
            treatmentHistoryEntry(
                setOf(treatment("1", isSystemic = true)),
                startYear = referenceDate.year - 3,
                startMonth = 1,
                stopYear = referenceDate.year - 3,
                stopMonth = 2
            ),
            treatmentHistoryEntry(
                setOf(treatment("2", isSystemic = true)),
                startYear = referenceDate.year - 3,
                startMonth = 6,
                stopYear = referenceDate.year - 3,
                stopMonth = 7
            ),
            treatmentHistoryEntry(
                setOf(treatment("3", isSystemic = true)),
                startYear = referenceDate.year - 2,
                startMonth = 5,
                stopYear = referenceDate.year
            ),
            treatmentHistoryEntry(setOf(treatment("4", isSystemic = true))),
        )
        val result = evaluateTreatmentTimingRelativeToNextLine(history, 6, referenceDate)
        assertThat(result.map { it.timing }).containsExactly(
            SystemicTreatmentAnalyser.TreatmentTiming.UNKNOWN,
            SystemicTreatmentAnalyser.TreatmentTiming.WITHIN,
            SystemicTreatmentAnalyser.TreatmentTiming.OUTSIDE,
            SystemicTreatmentAnalyser.TreatmentTiming.AMBIGUOUS
        )
    }

    @Test
    fun `partitionByIntent should place treatments with ignored intent in excluded partition`() {
        val curative = systemicTreatmentHistoryEntry.copy(intents = setOf(Intent.CURATIVE))
        val palliative = systemicTreatmentHistoryEntry.copy(intents = setOf(Intent.PALLIATIVE))
        val nullIntent = systemicTreatmentHistoryEntry.copy(intents = null)
        val (excluded, included) = partitionByIntent(
            listOf(curative, palliative, nullIntent),
            Intent.curativeAdjuvantNeoadjuvantSet()
        )
        assertThat(excluded).containsExactly(curative)
        assertThat(included).containsExactly(palliative, nullIntent)
    }

    @Test
    fun `partitionByIntent should place all treatments in included when no intents are ignored`() {
        val entries = listOf(
            systemicTreatmentHistoryEntry.copy(intents = setOf(Intent.CURATIVE)),
            systemicTreatmentHistoryEntry.copy(intents = null)
        )
        val (excluded, included) = partitionByIntent(entries, emptySet())
        assertThat(excluded).isEmpty()
        assertThat(included).containsExactlyElementsOf(entries)
    }

    @Test
    fun `partitionRecentTreatments should place treatments stopped within minDate in recent partition`() {
        val minDate = referenceDate.minusMonths(6)
        val recent = treatmentHistoryEntry(
            setOf(systemicTreatment),
            stopYear = referenceDate.minusMonths(1).year,
            stopMonth = referenceDate.minusMonths(1).monthValue
        )
        val nonRecent = treatmentHistoryEntry(
            setOf(systemicTreatment),
            stopYear = referenceDate.minusMonths(8).year,
            stopMonth = referenceDate.minusMonths(8).monthValue
        )
        val (recentPartition, nonRecentPartition) = partitionRecentTreatments(listOf(recent, nonRecent), minDate, false)
        assertThat(recentPartition).containsExactly(recent)
        assertThat(nonRecentPartition).containsExactly(nonRecent)
    }

    @Test
    fun `partitionRecentTreatments should place unknown-date treatments according to includeUnknown flag`() {
        val minDate = referenceDate.minusMonths(6)
        val unknownDate = treatmentHistoryEntry(setOf(systemicTreatment))

        val (recentWhenIncluded, nonRecentWhenIncluded) = partitionRecentTreatments(listOf(unknownDate), minDate, true)
        assertThat(recentWhenIncluded).containsExactly(unknownDate)
        assertThat(nonRecentWhenIncluded).isEmpty()

        val (recentWhenExcluded, nonRecentWhenExcluded) = partitionRecentTreatments(listOf(unknownDate), minDate, false)
        assertThat(recentWhenExcluded).isEmpty()
        assertThat(nonRecentWhenExcluded).containsExactly(unknownDate)
    }

    private fun assertNameForSystemicTreatmentHistoryEntry(
        treatmentHistory: List<TreatmentHistoryEntry>,
        name: String,
        treatmentSelector: (List<TreatmentHistoryEntry>) -> TreatmentHistoryEntry?
    ) {
        assertThat(treatmentSelector(treatmentHistory)?.treatments?.first()?.name).isEqualTo(name)
    }
}