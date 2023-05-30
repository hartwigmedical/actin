package com.hartwig.actin.treatment.ctc;

import java.util.List;
import java.util.Set;

import com.hartwig.actin.treatment.TestTrialData;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.ctc.config.ImmutableCTCDatabase;
import com.hartwig.actin.treatment.ctc.config.ImmutableCTCDatabaseEntry;

import org.jetbrains.annotations.NotNull;

public final class TestCTCModelFactory {

    private static final String METC_IGNORE_STUDY = "Ignore-Study";
    private static final int UNMAPPED_COHORT_ID = 3;

    @NotNull
    public static CTCModel createMinimalTestCTCDatabase() {
        return new CTCModel(ImmutableCTCDatabase.builder().build());
    }

    @NotNull
    public static CTCModel createProperTestCTCDatabase() {
        return new CTCModel(ImmutableCTCDatabase.builder()
                .entries(createTestCTCEntries())
                .studyMETCsToIgnore(Set.of(METC_IGNORE_STUDY))
                .unmappedCohortIds(Set.of(UNMAPPED_COHORT_ID))
                .build());
    }

    @NotNull
    private static List<CTCDatabaseEntry> createTestCTCEntries() {
        CTCDatabaseEntry study1Mapping1CohortA = ImmutableCTCDatabaseEntry.builder()
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

        CTCDatabaseEntry study1Mapping2CohortA = ImmutableCTCDatabaseEntry.builder()
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

        CTCDatabaseEntry study1UnmappedCohort = ImmutableCTCDatabaseEntry.builder()
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

        CTCDatabaseEntry study2Mapping = ImmutableCTCDatabaseEntry.builder()
                .studyId(2)
                .studyMETC(TestTrialData.TEST_TRIAL_ID_2)
                .studyAcronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_2)
                .studyTitle("Title-" + TestTrialData.TEST_TRIAL_ID_2)
                .studyStatus("Open")
                .build();

        CTCDatabaseEntry ignoreStudy = ImmutableCTCDatabaseEntry.builder()
                .studyId(3)
                .studyMETC(METC_IGNORE_STUDY)
                .studyAcronym("Acronym-Ignore")
                .studyTitle("Title-Ignore")
                .studyStatus("Open")
                .build();

        return List.of(study1Mapping1CohortA, study1Mapping2CohortA, study1UnmappedCohort, study2Mapping, ignoreStudy);
    }
}
