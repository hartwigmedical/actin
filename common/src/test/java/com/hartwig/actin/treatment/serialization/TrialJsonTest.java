package com.hartwig.actin.treatment.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
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
    public void canReadTreatmentDirectory() throws IOException {
        List<Trial> trials = TrialJson.readFromDir(TREATMENT_DIRECTORY);
        assertEquals(1, trials.size());

        assertTrial(trials.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotReadFilesFromNonDir() throws IOException {
        TrialJson.readFromDir(TREATMENT_DIRECTORY + "/file.json");
    }

    private static void assertTrial(@NotNull Trial trial) {
        assertEquals("test trial", trial.identification().trialId());
        assertEquals("TEST-TRIAL", trial.identification().acronym());
        assertEquals("This is a trial to test ACTIN", trial.identification().title());

        assertEquals(1, trial.generalEligibility().size());

        EligibilityFunction generalFunction = findBaseFunction(trial.generalEligibility(), EligibilityRule.IS_AT_LEAST_18_YEARS_OLD);
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = findCohort(trial.cohorts(), "A");
        assertTrue(cohortA.metadata().open());
        assertEquals("Cohort A", cohortA.metadata().description());
        assertEquals(1, cohortA.eligibility().size());

        EligibilityFunction functionA = findBaseFunction(cohortA.eligibility(), EligibilityRule.NOT);
        assertEquals(1, functionA.parameters().size());

        EligibilityFunction subFunctionA = findSubFunction(functionA.parameters(), EligibilityRule.HAS_ACTIVE_CNS_METASTASES);
        assertTrue(subFunctionA.parameters().isEmpty());

        Cohort cohortB = findCohort(trial.cohorts(), "B");
        assertTrue(cohortB.metadata().open());
        assertEquals("Cohort B", cohortB.metadata().description());
        assertTrue(cohortB.eligibility().isEmpty());

        Cohort cohortC = findCohort(trial.cohorts(), "C");
        assertFalse(cohortC.metadata().open());
        assertEquals("Cohort C", cohortC.metadata().description());
        assertEquals(3, cohortC.eligibility().size());

        EligibilityFunction functionC1 = findBaseFunction(cohortC.eligibility(), EligibilityRule.HAS_BIOPSY_AMENABLE_LESION);
        assertTrue(functionC1.parameters().isEmpty());

        EligibilityFunction functionC2 = findBaseFunction(cohortC.eligibility(), EligibilityRule.OR);
        assertEquals(2, functionC2.parameters().size());

        EligibilityFunction subFunction1 =
                findSubFunction(functionC2.parameters(), EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X);
        assertEquals(1, subFunction1.parameters().size());
        assertTrue(subFunction1.parameters().contains("0123"));

        EligibilityFunction subFunction2 = findSubFunction(functionC2.parameters(), EligibilityRule.IS_PREGNANT);
        assertTrue(subFunction2.parameters().isEmpty());

        EligibilityFunction functionC3 =
                findBaseFunction(cohortC.eligibility(), EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES);
        assertEquals(1, functionC3.parameters().size());
        assertTrue(functionC3.parameters().contains("2"));
    }

    @NotNull
    private static Cohort findCohort(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.metadata().cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with id: " + cohortId);
    }

    @NotNull
    private static EligibilityFunction findBaseFunction(@NotNull List<Eligibility> eligibility, @NotNull EligibilityRule rule) {
        for (Eligibility entry : eligibility) {
            if (entry.function().rule() == rule) {
                return entry.function();
            }
        }

        throw new IllegalStateException("Could not find base eligibility function with rule: " + rule);
    }

    @NotNull
    private static <X> EligibilityFunction findSubFunction(@NotNull List<X> functions, @NotNull EligibilityRule rule) {
        for (X function : functions) {
            EligibilityFunction func = (EligibilityFunction) function;
            if (func.rule() == rule) {
                return func;
            }
        }

        throw new IllegalStateException("Could not find sub function with rule: " + rule);
    }
}