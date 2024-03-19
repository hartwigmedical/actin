package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabase
import com.hartwig.actin.trial.status.TrialStatusEntry

object TestCTCDatabaseFactory {

    fun createMinimalTestCTCDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(emptyList(), emptySet(), emptySet(), emptySet())
    }

    fun createProperTestCTCDatabase(): TrialStatusDatabase {
        return TrialStatusDatabase(
            entries = createTestCTCEntries(),
            studyMETCsToIgnore = setOf(TestTrialData.TEST_TRIAL_METC_IGNORE),
            unmappedCohortIds = setOf(TestTrialData.TEST_UNMAPPED_COHORT_ID),
            mecStudiesNotInTrialStatusDatabase = setOf(TestTrialData.TEST_MEC_NOT_IN_CTC)
        )
    }

    private fun createTestCTCEntries(): List<TrialStatusEntry> {
        val study1Mapping1CohortA = TrialStatusEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_METC_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_METC_1,
            studyStatus = TrialStatus.OPEN,
            cohortId = 1,
            cohortName = "Cohort A-1",
            cohortStatus = TrialStatus.CLOSED,
            cohortSlotsNumberAvailable = 0
        )
        val study1Mapping2CohortA = TrialStatusEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_METC_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_METC_1,
            studyStatus = TrialStatus.OPEN,
            cohortId = 2,
            cohortName = "Cohort A-2",
            cohortStatus = TrialStatus.OPEN,
            cohortSlotsNumberAvailable = 5,
        )
        val study1UnmappedCohort = TrialStatusEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_METC_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_METC_1,
            studyStatus = TrialStatus.OPEN,
            cohortId = TestTrialData.TEST_UNMAPPED_COHORT_ID,
            cohortName = "Cohort D",
            cohortStatus = TrialStatus.OPEN,
            cohortSlotsNumberAvailable = 0,
        )
        val study2Mapping = TrialStatusEntry(
            studyId = 2,
            studyMETC = TestTrialData.TEST_TRIAL_METC_2,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_2,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_METC_2,
            studyStatus = TrialStatus.OPEN,
        )
        val ignoreStudy = TrialStatusEntry(
            studyId = 3,
            studyMETC = TestTrialData.TEST_TRIAL_METC_IGNORE,
            studyAcronym = "Acronym-Ignore",
            studyTitle = "Title-Ignore",
            studyStatus = TrialStatus.OPEN,
        )
        return listOf(study1Mapping1CohortA, study1Mapping2CohortA, study1UnmappedCohort, study2Mapping, ignoreStudy)
    }
}