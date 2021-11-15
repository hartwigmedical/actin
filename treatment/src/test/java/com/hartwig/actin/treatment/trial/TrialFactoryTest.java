package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory;

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
        TrialFactory factory = new TrialFactory(new TrialConfigModel(TestTrialConfigFactory.createProperTestTrialConfigDatabase()));
        List<Trial> trials = factory.create();

        assertEquals(1, trials.size());

        Trial trial = trials.get(0);
        assertEquals("TEST", trial.identification().trialId());
        assertEquals("Acronym-TEST", trial.identification().acronym());
        assertEquals("Title for TEST", trial.identification().title());

        assertEquals(1, trial.generalEligibility().size());

        EligibilityFunction generalFunction = findFunction(trial.generalEligibility(), EligibilityRule.IS_AT_LEAST_18_YEARS_OLD);
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = findCohort(trial.cohorts(), "A");
        assertEquals("Cohort A", cohortA.metadata().description());
        assertEquals(2, cohortA.eligibility().size());

        EligibilityFunction cohortFunction1 = findFunction(cohortA.eligibility(), EligibilityRule.HAS_INR_ULN_AT_MOST_X);
        assertEquals(1, cohortFunction1.parameters().size());
        assertTrue(cohortFunction1.parameters().contains("1"));

        EligibilityFunction cohortFunction2 = findFunction(cohortA.eligibility(), EligibilityRule.NOT);
        assertEquals(1, cohortFunction1.parameters().size());
        EligibilityFunction subFunction = (EligibilityFunction) cohortFunction2.parameters().get(0);
        assertEquals(EligibilityRule.OR, subFunction.rule());
        assertEquals(2, subFunction.parameters().size());

        Cohort cohortB = findCohort(trial.cohorts(), "B");
        assertEquals("Cohort B", cohortB.metadata().description());
        assertTrue(cohortB.eligibility().isEmpty());

        Cohort cohortC = findCohort(trial.cohorts(), "C");
        assertEquals("Cohort C", cohortC.metadata().description());
        assertTrue(cohortC.eligibility().isEmpty());
    }

    @NotNull
    private static Cohort findCohort(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.metadata().cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with ID: " + cohortId);
    }

    @NotNull
    private static EligibilityFunction findFunction(@NotNull List<Eligibility> eligibility, @NotNull EligibilityRule rule) {
        for (Eligibility entry : eligibility) {
            if (entry.function().rule() == rule) {
                return entry.function();
            }
        }

        throw new IllegalStateException("Could not find eligibility function with rule: " + rule);
    }
}