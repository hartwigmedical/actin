package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusResolverTest {

    private val entries = createTestEntries()

    @Test
    fun `Should assume cohort is closed and return validation error for non-existing cohortId`() {
        val doesNotExist = TestCohortDefinitionConfigFactory.MINIMAL.copy(cohortId = DOES_NOT_EXIST_COHORT_ID)
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
        assertThat(status.trialDatabaseValidationErrors).isEmpty()
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
        assertThat(statusClosed.trialDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should be able to determine status for single child consistent with parent`() {
        val status = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITH_SLOTS_COHORT_ID)
        )
        assertThatStatus(status, isOpen = true, hasSlotsAvailable = true)
        assertThat(status.cohortDefinitionErrors).isEmpty()
        assertThat(status.trialDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should stick to child when inconsistent with parent`() {
        val status = CohortStatusResolver.resolve(
            entries,
            cohortDefinitionConfig(CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID)
        )
        assertThatStatus(status, isOpen = true, hasSlotsAvailable = false)
        assertThat(status.cohortDefinitionErrors).isEmpty()
        assertThat(status.trialDatabaseValidationErrors).isEmpty()
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
        val config = cohortDefinitionConfig(DOES_NOT_EXIST_COHORT_ID)
        val (_, cohortValidation, _) = CohortStatusResolver.resolve(
            entries,
            config
        )
        assertThat(cohortValidation).containsExactly(
            CohortDefinitionValidationError(
                config = config,
                message = "Could not find trial status database entry with cohort ID '$DOES_NOT_EXIST_COHORT_ID'"
            ),
            CohortDefinitionValidationError(config = config, message = "Invalid cohort IDs configured for cohort")
        )
    }

    @Test
    fun `Should return validation error for each ancestor when multiple unrelated ancestors found for single set of children`() {
        val config = cohortDefinitionConfig(CHILD_OPEN_WITH_SLOTS_COHORT_ID, CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID)
        val wrongParent = TestTrialStatusDatabaseEntryFactory.createEntry(
            CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID,
            PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID,
            TrialStatus.OPEN,
            1
        )
        val correctParent = TestTrialStatusDatabaseEntryFactory.createEntry(
            CHILD_OPEN_WITH_SLOTS_COHORT_ID,
            PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
            TrialStatus.OPEN,
            1
        )
        val (_, _, trialStatusDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, TrialStatus.OPEN, 1),
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID, null, TrialStatus.OPEN, 1),
                correctParent,
                wrongParent
            ),
            config
        )
        assertThat(trialStatusDatabaseValidation).containsExactlyInAnyOrder(
            TrialStatusDatabaseValidationError(
                config = correctParent,
                message = "No common ancestor cohort found for cohorts [3, 4]"
            )
        )
    }

    @Test
    fun `Should not return validation errors if multiple cohorts have a common ancestor`() {
        val config = cohortDefinitionConfig(GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID, CHILD_OF_GRANDPARENT_OPEN_WITH_SLOTS_COHORT_ID)
        val statusInterpretation = CohortStatusResolver.resolve(entries, config)
        assertThatStatus(statusInterpretation, true, true)
        assertThat(statusInterpretation.trialDatabaseValidationErrors).isEmpty()
        assertThat(statusInterpretation.cohortDefinitionErrors).isEmpty()
    }

    @Test
    fun `Should return validation error when best child is open while parent is closed`() {
        val config = cohortDefinitionConfig(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID)
        val child = TestTrialStatusDatabaseEntryFactory.createEntry(
            PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID,
            PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
            TrialStatus.OPEN,
            1
        )
        val (_, _, trialStatusDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, TrialStatus.CLOSED, 1),
                child,
            ),
            config
        )
        assertThat(trialStatusDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = child,
                message = "Best child from IDs '[$PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID]' is open while parent with ID '$PARENT_1_OPEN_WITH_SLOTS_COHORT_ID' is closed"
            ),
        )
    }

    @Test
    fun `Should return validation error when best child from has slots available while parent has no slots available`() {
        val config = cohortDefinitionConfig(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID)
        val child = TestTrialStatusDatabaseEntryFactory.createEntry(
            PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID,
            PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
            TrialStatus.OPEN,
            1
        )
        val (_, _, trialDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, TrialStatus.OPEN, 0),
                child,
            ),
            config
        )
        assertThat(trialDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = child,
                message = "Best child from IDs '[$PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID]' has slots available while parent with ID '$PARENT_1_OPEN_WITH_SLOTS_COHORT_ID' has no slots available"
            ),
        )
    }

    @Test
    fun `Should return validation error when uninterpretable cohort status`() {
        val config = cohortDefinitionConfig(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID)
        val uninterpretable =
            TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, TrialStatus.UNINTERPRETABLE, 0)
        val (_, _, trialStatusDatabaseValidation) = CohortStatusResolver.resolve(
            listOf(
                uninterpretable,
            ),
            config
        )
        assertThat(trialStatusDatabaseValidation).containsExactly(
            TrialStatusDatabaseValidationError(
                config = uninterpretable,
                message = "Uninterpretable cohort status"
            ),
        )
    }

    @Test
    fun `Should consider no TrialStatusDatabaseEntry matches as invalid`() {
        assertThat(CohortStatusResolver.hasValidTrialStatusDatabaseMatches(emptyList())).isFalse
    }

    @Test
    fun `Should consider null TrialStatusDatabaseEntry match as invalid`() {
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

    private fun cohortDefinitionConfig(vararg trialStatusCohortIds: String) =
        TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = trialStatusCohortIds.toSet())

    companion object {
        private const val PARENT_1_OPEN_WITH_SLOTS_COHORT_ID = "1"
        private const val PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID = "2"
        private const val CHILD_OPEN_WITH_SLOTS_COHORT_ID = "3"
        private const val CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID = "4"
        private const val CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID = "5"
        private const val ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID = "6"
        private const val CHILD_FOR_PARENT_2_OPEN_WITH_SLOTS_COHORT_ID = "6"
        private const val DOES_NOT_EXIST_COHORT_ID = "7"
        private const val GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID = "8"
        private const val PARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID = "9"
        private const val GRANDPARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID = "10"
        private const val CHILD_OF_GRANDPARENT_OPEN_WITH_SLOTS_COHORT_ID = "11"

        private fun createTestEntries(): List<CohortStatusEntry> {
            val parentOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_1_OPEN_WITH_SLOTS_COHORT_ID, null, TrialStatus.OPEN, 1)
            val parentClosedWithoutSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID, null, TrialStatus.CLOSED, 0)
            val childOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    CHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )
            val childOpenWithoutSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    CHILD_OPEN_WITHOUT_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    0
                )
            val childClosedWithoutSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    CHILD_CLOSED_WITHOUT_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.CLOSED,
                    0
                )
            val anotherChildOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    ANOTHER_CHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_1_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )
            val childForParent2OpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    CHILD_FOR_PARENT_2_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_2_CLOSED_WITHOUT_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )
            val grandchildOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    PARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )
            val parentOfGrandchildOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    PARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    GRANDPARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )
            val grandparentOpenWithSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    GRANDPARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    null,
                    TrialStatus.OPEN,
                    1
                )
            val childOfGrandparentWithOpenSlots =
                TestTrialStatusDatabaseEntryFactory.createEntry(
                    CHILD_OF_GRANDPARENT_OPEN_WITH_SLOTS_COHORT_ID,
                    GRANDPARENT_FOR_GRANDCHILD_OPEN_WITH_SLOTS_COHORT_ID,
                    TrialStatus.OPEN,
                    1
                )

            return listOf(
                parentOpenWithSlots,
                parentClosedWithoutSlots,
                childOpenWithSlots,
                childOpenWithoutSlots,
                childClosedWithoutSlots,
                anotherChildOpenWithSlots,
                childForParent2OpenWithSlots,
                grandchildOpenWithSlots,
                parentOfGrandchildOpenWithSlots,
                grandparentOpenWithSlots,
                childOfGrandparentWithOpenSlots
            )
        }
    }
}