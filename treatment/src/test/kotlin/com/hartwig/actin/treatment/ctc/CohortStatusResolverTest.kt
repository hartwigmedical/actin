package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory.createEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusResolverTest {

    private val entries = createTestEntries()

    @Test
    fun shouldAssumeCohortIsClosedForNonExistingCohortId() {
        val status = CohortStatusResolver.resolve(entries, setOf(DOES_NOT_EXIST_COHORT_ID))
        assertThat(status.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleParent() {
        val statusOpen = CohortStatusResolver.resolve(entries, setOf(PARENT_OPEN_WITH_SLOTS_COHORT_ID))
        assertThat(statusOpen.open).isTrue
        assertThat(statusOpen.slotsAvailable).isTrue

        val statusClosed = CohortStatusResolver.resolve(entries, setOf(PARENT_CLOSED_WITHOUT_SLOTS_COHORT_ID))
        assertThat(statusClosed.open).isFalse
        assertThat(statusClosed.slotsAvailable).isFalse
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleChildConsistentWithParent() {
        val status = CohortStatusResolver.resolve(entries, setOf(CHILD_OPEN_WITH_SLOTS_COHORT_ID))
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isTrue
    }

    @Test
    fun shouldStickToChildWhenInconsistentWithParent() {
        val status = CohortStatusResolver.resolve(entries, setOf(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID))
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldBeAbleToDetermineStatusForMultipleChildrenConsistentWithParent() {
        val configuredCohortIds = setOf(CHILD_OPEN_WITH_SLOTS_COHORT_ID, ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID)
        val status = CohortStatusResolver.resolve(entries, configuredCohortIds)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isTrue
    }

    @Test
    fun shouldPickBestChildWhenBestChildIsInconsistentWithParent() {
        val configuredCohortIds = setOf(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID)
        val status = CohortStatusResolver.resolve(entries, configuredCohortIds)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun noCTCDatabaseEntryMatchesIsConsideredInvalid() {
        assertThat(CohortStatusResolver.hasValidCTCDatabaseMatches(emptyList())).isFalse
    }

    @Test
    fun nullCTCDatabaseEntryMatchIsConsideredInvalid() {
        assertThat(CohortStatusResolver.hasValidCTCDatabaseMatches(listOf(null))).isFalse
    }

    @Test
    fun matchesWithBothParentsAndChildAreConsideredInvalid() {
        assertThat(CohortStatusResolver.hasValidCTCDatabaseMatches(entries)).isFalse
    }

    @Test
    fun singleMatchIsAlwaysConsideredValid() {
        for (entry in entries) {
            assertThat(CohortStatusResolver.hasValidCTCDatabaseMatches(listOf(entry))).isTrue
        }
    }

    companion object {
        private const val PARENT_OPEN_WITH_SLOTS_COHORT_ID = 1
        private const val PARENT_CLOSED_WITHOUT_SLOTS_COHORT_ID = 2
        private const val CHILD_OPEN_WITH_SLOTS_COHORT_ID = 3
        private const val CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID = 4
        private const val CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID = 5
        private const val ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID = 6
        private const val DOES_NOT_EXIST_COHORT_ID = 7

        private fun createTestEntries(): List<CTCDatabaseEntry> {
            val parentOpenWithSlots = createEntry(PARENT_OPEN_WITH_SLOTS_COHORT_ID, null, "Open", 1)
            val parentClosedWithoutSlots = createEntry(PARENT_CLOSED_WITHOUT_SLOTS_COHORT_ID, null, "Gesloten", 0)
            val childOpenWithSlots = createEntry(CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1)
            val childOpenWithoutSlots = createEntry(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 0)
            val childClosedWithoutSlots = createEntry(CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Gesloten", 0)
            val anotherChildOpenWithSlots =
                createEntry(ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1)

            return listOf(
                parentOpenWithSlots,
                parentClosedWithoutSlots,
                childOpenWithSlots,
                childOpenWithoutSlots,
                childClosedWithoutSlots,
                anotherChildOpenWithSlots
            )
        }
    }
}