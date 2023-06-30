package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.CohortStatusInterpreter.interpret
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory.createEntry
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory
import org.junit.Assert
import org.junit.Test

class CohortStatusInterpreterTest {
    private val entries = createTestEntries()

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsNotAvailable() {
        val notAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE)
        Assert.assertNull(interpret(entries, notAvailable))
    }

    @Test
    fun shouldAssumeUnexplainedMissingCohortsAreClosed() {
        val unexplainedMissing = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY)
        val status = interpret(entries, unexplainedMissing)
        Assert.assertFalse(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
    }

    @Test
    fun shouldAssumeUnmappedClosedCohortsAreClosed() {
        val notMappedClosed = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED)
        val status = interpret(entries, notMappedClosed)
        Assert.assertFalse(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
    }

    @Test
    fun shouldAssumeUnmappedUnavailableCohortsAreClosed() {
        val notMappedNotAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
        val status = interpret(entries, notMappedNotAvailable)
        Assert.assertFalse(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
    }

    @Test
    fun shouldAssumeCohortIsClosedForInvalidCohortConfig() {
        val idDoesNotExist = createWithCTCCohortIDs("12")
        val status = interpret(entries, idDoesNotExist)
        Assert.assertFalse(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleParent() {
        val config = createWithCTCCohortIDs(PARENT_OPEN_WITH_SLOTS_COHORT_ID.toString())
        val status = interpret(entries, config)
        Assert.assertTrue(status!!.open)
        Assert.assertTrue(status.slotsAvailable)
    }

    @Test
    fun shouldBeAbleToDetermineStatusForSingleChildConsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString())
        val status = interpret(entries, config)
        Assert.assertTrue(status!!.open)
        Assert.assertTrue(status.slotsAvailable)
    }

    @Test
    fun shouldStickToChildWhenInconsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID.toString())
        val status = interpret(entries, config)
        Assert.assertTrue(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
    }

    @Test
    fun shouldBeAbleToDetermineStatusForMultipleChildrenConsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString(), ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID.toString())
        val status = interpret(entries, config)
        Assert.assertTrue(status!!.open)
        Assert.assertTrue(status.slotsAvailable)
    }

    @Test
    fun shouldPickBestChildWhenBestChildIsInconsistentWithParent() {
        val config = createWithCTCCohortIDs(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID.toString(), CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID.toString())
        val status = interpret(entries, config)
        Assert.assertTrue(status!!.open)
        Assert.assertFalse(status.slotsAvailable)
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