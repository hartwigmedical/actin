package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory.createEntry
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCDatabaseEntryInterpreterTest {

    private val entries = createTestEntries()

    @Test
    fun shouldAssumeCohortIsClosedForInvalidCohortConfig() {
        val idDoesNotExist = createWithCTCCohortIDs("12")
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, idDoesNotExist)
        assertThat(status.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleParent() {
        val config = createWithCTCCohortIDs(PARENT_OPEN_WITH_SLOTS_COHORT_ID.toString())
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, config)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isTrue
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleChildConsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString())
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, config)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isTrue
    }

    @Test
    fun shouldStickToChildWhenInconsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID.toString())
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, config)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldBeAbleToDetermineStatusForMultipleChildrenConsistentWithParent() {
        val config = createWithCTCCohortIDs(
            CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString(),
            ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString()
        )
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, config)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isTrue
    }

    @Test
    fun shouldPickBestChildWhenBestChildIsInconsistentWithParent() {
        val config = createWithCTCCohortIDs(
            CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID.toString(),
            CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID.toString()
        )
        val status = CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, config)
        assertThat(status.open).isTrue
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun noMatchIsConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(emptyList())).isFalse
    }

    @Test
    fun nullMatchIsConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(listOf(null))).isFalse
    }

    @Test
    fun entriesWithBothParentsAndChildAreConsideredInvalid() {
        assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(entries)).isFalse
    }

    @Test
    fun singleEntryIsAlwaysConsideredValid() {
        for (entry in entries) {
            assertThat(CTCDatabaseEntryInterpreter.hasValidCTCDatabaseMatches(listOf(entry))).isTrue
        }
    }

    companion object {
        private const val PARENT_OPEN_WITH_SLOTS_COHORT_ID = 1
        private const val CHILD_OPEN_WITH_SLOTS_COHORT_ID = 2
        private const val CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID = 3
        private const val CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID = 4
        private const val ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID = 5

        private fun createTestEntries(): List<CTCDatabaseEntry> {
            val parentOpenWithSlots = createEntry(PARENT_OPEN_WITH_SLOTS_COHORT_ID, null, "Open", 1)
            val childOpenWithSlots = createEntry(CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1)
            val childOpenWithoutSlots = createEntry(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 0)
            val childClosedWithoutSlots = createEntry(CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Gesloten", 0)
            val anotherChildOpenWithSlots =
                createEntry(ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1)

            return listOf(
                parentOpenWithSlots,
                childOpenWithSlots,
                childOpenWithoutSlots,
                childClosedWithoutSlots,
                anotherChildOpenWithSlots
            )
        }

        private fun createWithCTCCohortIDs(vararg ctcCohortIDs: String): CohortDefinitionConfig {
            return TestCohortDefinitionConfigFactory.MINIMAL.copy(ctcCohortIds = setOf(*ctcCohortIDs))
        }
    }
}