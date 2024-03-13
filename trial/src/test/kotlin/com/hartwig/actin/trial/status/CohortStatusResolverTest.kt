package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.status.config.TestCTCDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusResolverTest {

    private val entries = createTestEntries()

    @Test
    fun `Should assume cohort is closed and return validation error for non-existing cohortId`() {
        val doesNotExist = TestCohortDefinitionConfigFactory.MINIMAL.copy(cohortId = DOES_NOT_EXIST_COHORT_ID.toString())
        val status =
            CohortStatusResolver.resolve(
                entries,
                doesNotExist
            )
        assertThatStatus(status, isOpen = false, hasSlotsAvailable = false)
        assertThat(status.cohortDefinitionErrors).containsExactly(
            CohortDefinitionValidationError(
                config = doesNotExist,
                message = "Invalid cohort IDs configured for cohort"
            )
        )
        assertThat(status.ctcDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should be able to determine status for single parent`() {
        val statusOpen = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID)
        )
        assertThatStatus(statusOpen, isOpen = true, hasSlotsAvailable = true)

        val statusClosed = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID)
        )
        assertThatStatus(statusClosed, isOpen = false, hasSlotsAvailable = false)
        assertThat(statusClosed.cohortDefinitionErrors).isEmpty()
        assertThat(statusClosed.ctcDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should be able to determine status for single child consistent with parent`() {
        val status = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITH_SLOTS_COHORT_ID)
        )
        assertThatStatus(status, isOpen = true, hasSlotsAvailable = true)
        assertThat(status.cohortDefinitionErrors).isEmpty()
        assertThat(status.ctcDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should stick to child when inconsistent with parent`() {
        val status = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID)
        )
        assertThatStatus(status, isOpen = true, hasSlotsAvailable = false)
        assertThat(status.cohortDefinitionErrors).isEmpty()
        assertThat(status.ctcDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should be able to determine status for multiple children consistent with parent`() {
        val status = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITH_SLOTS_COHORT_ID, ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID)
        )
        assertThatStatus(status, isOpen = true, hasSlotsAvailable = true)
        assertThat(status.cohortDefinitionErrors).isEmpty()
    }

    @Test
    fun `Should pick best child when best child is inconsistent with parent`() {
        val noSlotsStatus = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID)
        )
        assertThatStatus(noSlotsStatus, isOpen = true, hasSlotsAvailable = false)

        val openStatus = CohortStatusResolver.resolve(entries, cohortDefinitionConfig(CHILD_FOR_PARENT_2_OPEN_WITH_SLOTS_COHORT_ID))
        assertThatStatus(openStatus, isOpen = true, hasSlotsAvailable = true)
        assertThat(openStatus.cohortDefinitionErrors).isEmpty()
    }

    @Test
    fun `Should return validation errors when invalid cohort IDs configured for cohort`() {
        val config = cohortDefinitionConfig(8)
        val (_, cohortValidation, _) = CohortStatusResolver.resolve(
            entries,
            config
        )
        assertThat(cohortValidation).containsExactly(
            CohortDefinitionValidationError(config = config, message = "Could not find CTC database entry with cohort ID '8'"),
            CohortDefinitionValidationError(config = config, message = "Invalid cohort IDs configured for cohort")
        )
    }

    @Test
    fun `Should return validation error when multiple parents found for single set of children`() {
        val config = cohortDefinitionConfig(3, 4)
        val wrongParent = TestCTCDatabaseEntryFactory.createEntry(4, 2, "Open", 1)
        val (_, _, ctcDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestCTCDatabaseEntryFactory.createEntry(1, null, "Open", 1),
                TestCTCDatabaseEntryFactory.createEntry(2, null, "Open", 1),
                TestCTCDatabaseEntryFactory.createEntry(3, 1, "Open", 1),
                wrongParent
            ),
            config
        )
        assertThat(ctcDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = wrongParent,
                message = "Multiple parents found for single set of children"
            ),
        )
    }

    @Test
    fun `Should return validation error when best child is open while parent is closed`() {
        val config = cohortDefinitionConfig(2)
        val child = TestCTCDatabaseEntryFactory.createEntry(2, 1, "Open", 1)
        val (_, _, ctcDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestCTCDatabaseEntryFactory.createEntry(1, null, "Closed", 1),
                child,
            ),
            config
        )
        assertThat(ctcDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = child,
                message = "Best child from IDs '[2]' is open while parent with ID '1' is closed"
            ),
        )
    }

    @Test
    fun `Should return validation error when best child from has slots available while parent has no slots available`() {
        val config = cohortDefinitionConfig(2)
        val child = TestCTCDatabaseEntryFactory.createEntry(2, 1, "Open", 1)
        val (_, _, ctcDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestCTCDatabaseEntryFactory.createEntry(1, null, "Open", 0),
                child,
            ),
            config
        )
        assertThat(ctcDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = child,
                message = "Best child from IDs '[2]' has slots available while parent with ID '1' has no slots available"
            ),
        )
    }

    @Test
    fun `Should return validation error when  no cohort status available in CTC for cohort`() {
        val config = cohortDefinitionConfig(1)
        val noStatus = TestCTCDatabaseEntryFactory.createEntry(1, null, null, 0)
        val (_, _, ctcDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                noStatus,
            ),
            config
        )
        assertThat(ctcDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = noStatus,
                message = "No cohort status available in CTC for cohort"
            ),
        )
    }

    @Test
    fun `Should return validation error when uninterpretable cohort status`() {
        val config = cohortDefinitionConfig(1)
        val uninterpretable = TestCTCDatabaseEntryFactory.createEntry(1, null, "Uninterpretable", 0)
        val (_, _, ctcDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                uninterpretable,
            ),
            config
        )
        assertThat(ctcDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = uninterpretable,
                message = "Uninterpretable cohort status"
            ),
        )
    }

    @Test
    fun `Should consider no CTCDatabaseEntry matches as invalid`() {
        assertThat(CohortStatusResolver.hasValidTrialStatusDatabaseMatches(emptyList())).isFalse
    }

    @Test
    fun `Should consider null CTCDatabaseEntry match as invalid`() {
        assertThat(CohortStatusResolver.hasValidTrialStatusDatabaseMatches(listOf(null))).isFalse
    }

    @Test
    fun `Should consider matches with both parents and child as invalid`() {
        assertThat(CohortStatusResolver.hasValidTrialStatusDatabaseMatches(entries)).isFalse
    }

    @Test
    fun `Should consider single match is always considered valid`() {
        for (entry in entries) {
            assertThat(CohortStatusResolver.hasValidTrialStatusDatabaseMatches(listOf(entry))).isTrue
        }
    }

    private fun assertThatStatus(status: CohortStatusInterpretation, isOpen: Boolean, hasSlotsAvailable: Boolean) {
        val interpretedCohortStatus = status.status!!
        assertThat(interpretedCohortStatus.open).isEqualTo(isOpen)
        assertThat(interpretedCohortStatus.slotsAvailable).isEqualTo(hasSlotsAvailable)
    }

    private fun cohortDefinitionConfig(vararg ctcCohortIds: Int) =
        TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = ctcCohortIds.map(Int::toString).toSet())

    companion object {
        private const val PARENT_1_OPEN_WITH_SLOTS_COHORT_ID = 1
        private const val PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID = 2
        private const val CHILD_OPEN_WITH_SLOTS_COHORT_ID = 3
        private const val CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID = 4
        private const val CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID = 5
        private const val ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID = 6
        private const val CHILD_FOR_PARENT_2_OPEN_WITH_SLOTS_COHORT_ID = 6
        private const val DOES_NOT_EXIST_COHORT_ID = 7

        private fun createTestEntries(): List<TrialStatusEntry> {
            val parentOpenWithSlots = TestCTCDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, "Open", 1)
            val parentClosedWithoutSlots =
                TestCTCDatabaseEntryFactory.createEntry(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID, null, "Gesloten", 0)
            val childOpenWithSlots =
                TestCTCDatabaseEntryFactory.createEntry(CHILD_OPEN_WITH_SLOTS_COHORT_ID, PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, "Open", 1)
            val childOpenWithoutSlots =
                TestCTCDatabaseEntryFactory.createEntry(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID, PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, "Open", 0)
            val childClosedWithoutSlots =
                TestCTCDatabaseEntryFactory.createEntry(
                    CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    "Gesloten",
                    0
                )
            val anotherChildOpenWithSlots =
                TestCTCDatabaseEntryFactory.createEntry(
                    ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    "Open",
                    1
                )
            val childForParent2OpenWithSlots =
                TestCTCDatabaseEntryFactory.createEntry(
                    CHILD_FOR_PARENT_2_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID,
                    "Open",
                    1
                )

            return listOf(
                parentOpenWithSlots,
                parentClosedWithoutSlots,
                childOpenWithSlots,
                childOpenWithoutSlots,
                childClosedWithoutSlots,
                anotherChildOpenWithSlots,
                childForParent2OpenWithSlots
            )
        }
    }
}