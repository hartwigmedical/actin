package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialFactoryTest {

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
        assertEquals(EligibilityRule.IS_ADULT, generalFunction.rule());
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = find(trial.cohorts(), "A");
        assertEquals("Cohort A", cohortA.description());
        assertEquals(1, cohortA.eligibilityFunctions().size());

        EligibilityFunction cohortFunction = cohortA.eligibilityFunctions().get(0);
        assertEquals(EligibilityRule.IS_ADULT, cohortFunction.rule());
        assertTrue(cohortFunction.parameters().isEmpty());

        Cohort cohortB = find(trial.cohorts(), "B");
        assertEquals("Cohort B", cohortB.description());
        assertTrue(cohortB.eligibilityFunctions().isEmpty());

        Cohort cohortC = find(trial.cohorts(), "C");
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

        throw new IllegalStateException("Could not find cohort with ID: " + cohortId);
    }
}