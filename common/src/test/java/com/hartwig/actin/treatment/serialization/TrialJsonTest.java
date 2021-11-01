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
        assertEquals("test trial", trial.trialId());
        assertEquals("TEST-TRIAL", trial.acronym());
        assertEquals("This is a trial to test ACTIN", trial.title());

        assertEquals(1, trial.generalEligibilityFunctions().size());

        EligibilityFunction generalFunction = findFunction(trial.generalEligibilityFunctions(), EligibilityRule.IS_AT_LEAST_18_YEARS_OLD);
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = findCohort(trial.cohorts(), "A");
        assertTrue(cohortA.open());
        assertEquals("Cohort A", cohortA.description());
        assertEquals(1, cohortA.eligibilityFunctions().size());

        EligibilityFunction functionA = findFunction(cohortA.eligibilityFunctions(), EligibilityRule.NOT);
        assertEquals(1, functionA.parameters().size());

        EligibilityFunction subFunctionA = findFunction(functionA.parameters(), EligibilityRule.HAS_ACTIVE_CNS_METASTASES);
        assertTrue(subFunctionA.parameters().isEmpty());

        Cohort cohortB = findCohort(trial.cohorts(), "B");
        assertTrue(cohortB.open());
        assertEquals("Cohort B", cohortB.description());
        assertTrue(cohortB.eligibilityFunctions().isEmpty());

        Cohort cohortC = findCohort(trial.cohorts(), "C");
        assertFalse(cohortC.open());
        assertEquals("Cohort C", cohortC.description());
        assertEquals(3, cohortC.eligibilityFunctions().size());

        EligibilityFunction functionC1 = findFunction(cohortC.eligibilityFunctions(), EligibilityRule.HAS_BIOPSY_AMENABLE_LESION);
        assertTrue(functionC1.parameters().isEmpty());

        EligibilityFunction functionC2 = findFunction(cohortC.eligibilityFunctions(), EligibilityRule.OR);
        assertEquals(2, functionC2.parameters().size());

        EligibilityFunction subFunction1 = findFunction(functionC2.parameters(), EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X);
        assertEquals(1, subFunction1.parameters().size());
        assertTrue(subFunction1.parameters().contains("0123"));

        EligibilityFunction subFunction2 = findFunction(functionC2.parameters(), EligibilityRule.IS_PREGNANT);
        assertTrue(subFunction2.parameters().isEmpty());

        EligibilityFunction functionC3 =
                findFunction(cohortC.eligibilityFunctions(), EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES);
        assertEquals(1, functionC3.parameters().size());
        assertTrue(functionC3.parameters().contains("2"));
    }

    @NotNull
    private static Cohort findCohort(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with id: " + cohortId);
    }

    @NotNull
    private static <X> EligibilityFunction findFunction(@NotNull List<X> functions, @NotNull EligibilityRule rule) {
        for (X function : functions) {
            EligibilityFunction func = (EligibilityFunction) function;
            if (func.rule() == rule) {
                return func;
            }
        }

        throw new IllegalStateException("Could not find function with rule: " + rule);
    }
}