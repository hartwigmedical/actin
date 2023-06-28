package com.hartwig.actin.treatment.ctc.config;

import java.util.List;
import java.util.Set;

import com.hartwig.actin.treatment.TestTrialData;

import org.jetbrains.annotations.NotNull;

public final class TestCTCDatabaseFactory {

    private static final String METC_IGNORE_STUDY = "Ignore-Study";
    private static final int UNMAPPED_COHORT_ID = 3;

    @NotNull
    public static CTCDatabase createMinimalTestCTCDatabase() {
        return ImmutableCTCDatabase.builder().build();
    }

    @NotNull
    public static CTCDatabase createProperTestCTCDatabase() {
        return ImmutableCTCDatabase.builder()
                .entries(createTestCTCEntries())
                .studyMETCsToIgnore(Set.of(METC_IGNORE_STUDY))
                .unmappedCohortIds(Set.of(UNMAPPED_COHORT_ID))
                .build();
    }

    @NotNull
    private static List<CTCDatabaseEntry> createTestCTCEntries() {
        CTCDatabaseEntry study1Mapping1CohortA = TestCTCDatabaseEntryFactory.builder()
                .studyId(1)
                .studyMETC(TestTrialData.TEST_TRIAL_ID_1)
                .studyAcronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyTitle("Title-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyStatus("Open")
                .cohortId(1)
                .cohortName("Cohort A-1")
                .cohortStatus("Gesloten")
                .cohortSlotsNumberAvailable(0)
                .build();

        CTCDatabaseEntry study1Mapping2CohortA = TestCTCDatabaseEntryFactory.builder()
                .studyId(1)
                .studyMETC(TestTrialData.TEST_TRIAL_ID_1)
                .studyAcronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyTitle("Title-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyStatus("Open")
                .cohortId(2)
                .cohortName("Cohort A-2")
                .cohortStatus("Open")
                .cohortSlotsNumberAvailable(5)
                .build();

        CTCDatabaseEntry study1UnmappedCohort = TestCTCDatabaseEntryFactory.builder()
                .studyId(1)
                .studyMETC(TestTrialData.TEST_TRIAL_ID_1)
                .studyAcronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyTitle("Title-" + TestTrialData.TEST_TRIAL_ID_1)
                .studyStatus("Open")
                .cohortId(UNMAPPED_COHORT_ID)
                .cohortName("Cohort D")
                .cohortStatus("Open")
                .cohortSlotsNumberAvailable(0)
                .build();

        CTCDatabaseEntry study2Mapping = TestCTCDatabaseEntryFactory.builder()
                .studyId(2)
                .studyMETC(TestTrialData.TEST_TRIAL_ID_2)
                .studyAcronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_2)
                .studyTitle("Title-" + TestTrialData.TEST_TRIAL_ID_2)
                .studyStatus("Open")
                .build();

        CTCDatabaseEntry ignoreStudy = TestCTCDatabaseEntryFactory.builder()
                .studyId(3)
                .studyMETC(METC_IGNORE_STUDY)
                .studyAcronym("Acronym-Ignore")
                .studyTitle("Title-Ignore")
                .studyStatus("Open")
                .build();

        return List.of(study1Mapping1CohortA, study1Mapping2CohortA, study1UnmappedCohort, study2Mapping, ignoreStudy);
    }
}
