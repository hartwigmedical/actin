package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.status.CohortStatusEntry
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabase

object TestTrialStatusDatabaseFactory {

    fun createMinimalTestTrialStatusDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(emptyList(), emptySet(), emptySet(), emptySet())
    }

    fun createProperTestTrialStatusDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(
            entries = createTestTrialStatusEntries(),
            studyMETCsToIgnore = setOf(TestTrialData.TEST_TRIAL_METC_IGNORE),
            unmappedCohortIds = setOf(TestTrialData.TEST_UNMAPPED_COHORT_ID),
            studiesNotInTrialStatusDatabase = setOf(TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE)
        )
    }

    private fun createTestTrialStatusEntries(): List<CohortStatusEntry> {
        val study1Mapping1CohortA = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_METC_1,
            trialStatus = TrialStatus.OPEN,
            cohortId = "1",
            cohortStatus = TrialStatus.CLOSED,
            cohortSlotsNumberAvailable = 0
        )
        val study1Mapping2CohortA = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_METC_1,
            trialStatus = TrialStatus.OPEN,
            cohortId = "2",
            cohortStatus = TrialStatus.OPEN,
            cohortSlotsNumberAvailable = 5,
        )
        val study1UnmappedCohort = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_METC_1,
            trialStatus = TrialStatus.OPEN,
            cohortId = TestTrialData.TEST_UNMAPPED_COHORT_ID,
            cohortStatus = TrialStatus.OPEN,
            cohortSlotsNumberAvailable = 0,
        )
        val study2Mapping = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_METC_2,
            trialStatus = TrialStatus.OPEN,
            cohortId = "",
            cohortStatus = TrialStatus.OPEN,
        )
        val ignoreStudy = CohortStatusEntry(
            nctId = TestTrialData.TEST_TRIAL_METC_IGNORE,
            trialStatus = TrialStatus.OPEN,
            cohortId = "",
            cohortStatus = TrialStatus.OPEN,
        )
        return listOf(study1Mapping1CohortA, study1Mapping2CohortA, study1UnmappedCohort, study2Mapping, ignoreStudy)
    }
}