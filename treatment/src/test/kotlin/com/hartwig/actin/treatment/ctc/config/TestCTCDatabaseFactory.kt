package com.hartwig.actin.treatment.ctc.config

import com.hartwig.actin.treatment.TestTrialData

object TestCTCDatabaseFactory {
    private const val METC_IGNORE_STUDY = "Ignore-Study"
    private const val UNMAPPED_COHORT_ID = 3

    fun createMinimalTestCTCDatabase(): CTCDatabase {
        return CTCDatabase(emptyList(), emptySet(), emptySet())
    }

    fun createProperTestCTCDatabase(): CTCDatabase {
        return CTCDatabase(
            entries = createTestCTCEntries(),
            studyMETCsToIgnore = setOf(METC_IGNORE_STUDY),
            unmappedCohortIds = setOf(UNMAPPED_COHORT_ID)
        )
    }

    private fun createTestCTCEntries(): List<CTCDatabaseEntry> {
        val study1Mapping1CohortA = CTCDatabaseEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_ID_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_ID_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_ID_1,
            studyStatus = "Open",
            cohortId = 1,
            cohortName = "Cohort A-1",
            cohortStatus = "Gesloten",
            cohortSlotsNumberAvailable = 0
        )
        val study1Mapping2CohortA = CTCDatabaseEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_ID_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_ID_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_ID_1,
            studyStatus = "Open",
            cohortId = 2,
            cohortName = "Cohort A-2",
            cohortStatus = "Open",
            cohortSlotsNumberAvailable = 5,
        )
        val study1UnmappedCohort = CTCDatabaseEntry(
            studyId = 1,
            studyMETC = TestTrialData.TEST_TRIAL_ID_1,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_ID_1,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_ID_1,
            studyStatus = "Open",
            cohortId = UNMAPPED_COHORT_ID,
            cohortName = "Cohort D",
            cohortStatus = "Open",
            cohortSlotsNumberAvailable = 0,
        )
        val study2Mapping = CTCDatabaseEntry(
            studyId = 2,
            studyMETC = TestTrialData.TEST_TRIAL_ID_2,
            studyAcronym = "Acronym-" + TestTrialData.TEST_TRIAL_ID_2,
            studyTitle = "Title-" + TestTrialData.TEST_TRIAL_ID_2,
            studyStatus = "Open",
        )
        val ignoreStudy = CTCDatabaseEntry(
            studyId = 3,
            studyMETC = METC_IGNORE_STUDY,
            studyAcronym = "Acronym-Ignore",
            studyTitle = "Title-Ignore",
            studyStatus = "Open",
        )
        return listOf(study1Mapping1CohortA, study1Mapping2CohortA, study1UnmappedCohort, study2Mapping, ignoreStudy)
    }
}