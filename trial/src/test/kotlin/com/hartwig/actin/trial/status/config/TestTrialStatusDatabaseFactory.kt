package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.status.CohortStatusEntry
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabase

object TestTrialStatusDatabaseFactory {

    fun createMinimalTestTrialStatusDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(emptyList())
    }

    fun createProperTestTrialStatusDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(
            entries = createTestTrialStatusEntries(),
        )
    }

    private fun createTestTrialStatusEntries(): List<CohortStatusEntry> {
        val study1Mapping1CohortA = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_NCT_1,
            trialStatus = TrialStatus.OPEN,
            cohortId = "1",
            cohortStatus = TrialStatus.CLOSED,
            cohortSlotsNumberAvailable = 0
        )
        val study1Mapping2CohortA = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_NCT_1,
            trialStatus = TrialStatus.OPEN,
            cohortId = "2",
            cohortStatus = TrialStatus.OPEN,
            cohortSlotsNumberAvailable = 5,
        )
        val study2Mapping = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_NCT_2,
            trialStatus = TrialStatus.OPEN,
            cohortId = "",
            cohortStatus = TrialStatus.OPEN,
        )
        return listOf(study1Mapping1CohortA, study1Mapping2CohortA, study2Mapping)
    }
}