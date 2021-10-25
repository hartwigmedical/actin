package com.hartwig.actin.treatment.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialJsonTest {

    private static final String TREATMENT_DIRECTORY = Resources.getResource("treatment").getPath();

    @Test
    public void canConvertBackAndForthJson() {
        Trial minimal = TestTreatmentFactory.createMinimalTestTrial();
        Trial convertedMinimal = TrialJson.fromJson(TrialJson.toJson(minimal));

        assertEquals(minimal, convertedMinimal);

        Trial proper = TestTreatmentFactory.createProperTestTrial();
        Trial convertedProper = TrialJson.fromJson(TrialJson.toJson(proper));

        assertEquals(proper, convertedProper);
    }

    @Test
    public void canReadClinicalRecordDirectory() throws IOException {
        List<Trial> trials = TrialJson.readFromDir(TREATMENT_DIRECTORY);
        assertEquals(1, trials.size());

        assertTrial(trials.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotReadFilesFromNonDir() throws IOException {
        TrialJson.readFromDir(TREATMENT_DIRECTORY + "/file.json");
    }

    private static void assertTrial(@NotNull Trial trial) {
        assertEquals("test trial", trial.trialId());
        assertEquals("TEST-TRIAL", trial.acronym());
        assertEquals("This is a trial to test ACTIN", trial.title());

        assertEquals(1, trial.generalEligibilityFunctions().size());

        EligibilityFunction generalFunction = trial.generalEligibilityFunctions().get(0);
        assertEquals(EligibilityRule.IS_ADULT, generalFunction.rule());
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = find(trial.cohorts(), "A");
        assertTrue(cohortA.open());
        assertEquals("Cohort A", cohortA.description());
        assertTrue(cohortA.eligibilityFunctions().isEmpty());

        Cohort cohortB = find(trial.cohorts(), "B");
        assertTrue(cohortB.open());
        assertEquals("Cohort B", cohortB.description());
        assertTrue(cohortB.eligibilityFunctions().isEmpty());

        Cohort cohortC = find(trial.cohorts(), "C");
        assertFalse(cohortC.open());
        assertEquals("Cohort C", cohortC.description());
        assertTrue(cohortC.eligibilityFunctions().isEmpty());
    }

    @NotNull
    private static Cohort find(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with id: " + cohortId);
    }
}