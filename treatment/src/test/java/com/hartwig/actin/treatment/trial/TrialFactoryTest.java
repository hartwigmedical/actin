package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialFactoryTest {

    private static final String TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").getPath();

    @Test
    public void canCreateFromTrialConfigDirectory() throws IOException {
        assertNotNull(TrialFactory.fromTrialConfigDirectory(TRIAL_CONFIG_DIRECTORY));
    }

    @Test
    public void canCreateFromProperTestModel() {
        List<Trial> trials = new TrialFactory(TestTrialConfigFactory.createProperTestTrialConfigModel()).create();

        assertEquals(1, trials.size());

        Trial trial = trials.get(0);
        assertEquals("TEST", trial.trialId());
        assertEquals("Acronym-TEST", trial.acronym());
        assertEquals("Title for TEST", trial.title());

        assertEquals(1, trial.generalEligibilityFunctions().size());

        EligibilityFunction generalFunction = trial.generalEligibilityFunctions().get(0);
        assertEquals(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, generalFunction.rule());
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = findCohort(trial.cohorts(), "A");
        assertEquals("Cohort A", cohortA.description());
        assertEquals(2, cohortA.eligibilityFunctions().size());

        EligibilityFunction cohortFunction1 = findFunction(cohortA.eligibilityFunctions(), EligibilityRule.HAS_INR_ULN_AT_MOST_X);
        assertEquals(1, cohortFunction1.parameters().size());
        assertTrue(cohortFunction1.parameters().contains("1"));

        EligibilityFunction cohortFunction2 = findFunction(cohortA.eligibilityFunctions(), EligibilityRule.NOT);
        assertEquals(1, cohortFunction1.parameters().size());
        EligibilityFunction subFunction = (EligibilityFunction) cohortFunction2.parameters().get(0);
        assertEquals(EligibilityRule.OR, subFunction.rule());
        assertEquals(2, subFunction.parameters().size());

        Cohort cohortB = findCohort(trial.cohorts(), "B");
        assertEquals("Cohort B", cohortB.description());
        assertTrue(cohortB.eligibilityFunctions().isEmpty());

        Cohort cohortC = findCohort(trial.cohorts(), "C");
        assertEquals("Cohort C", cohortC.description());
        assertTrue(cohortC.eligibilityFunctions().isEmpty());
    }

    @NotNull
    private static Cohort findCohort(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with ID: " + cohortId);
    }

    @NotNull
    private static EligibilityFunction findFunction(@NotNull List<EligibilityFunction> functions, @NotNull EligibilityRule rule) {
        for (EligibilityFunction function : functions) {
            if (function.rule() == rule) {
                return function;
            }
        }

        throw new IllegalStateException("Could not find eligibility function with rule: " + rule);

    }
}