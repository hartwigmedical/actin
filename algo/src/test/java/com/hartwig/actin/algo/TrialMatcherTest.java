package com.hartwig.actin.algo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.SampleTreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialMatcherTest {

    @Test
    public void canMatchTrialsOnProperTestData() {
        PatientRecord patient = TestDataFactory.createTestPatientRecord();
        Trial trial = TestTreatmentFactory.createProperTestTrial();

        SampleTreatmentMatch match = TrialMatcher.determineEligibility(patient, Lists.newArrayList(trial));

        assertEquals(match.sampleId(), patient.sampleId());
        assertEquals(1, match.trialMatches().size());

        assertTrialMatch(match.trialMatches().get(0));
    }

    private static void assertTrialMatch(@NotNull TrialEligibility trialEligibility) {
        assertEquals(1, trialEligibility.evaluations().size());
        assertEquals(Evaluation.PASS, trialEligibility.overallEvaluation());
        assertEquals(Evaluation.PASS, find(trialEligibility.evaluations(), EligibilityRule.IS_AT_LEAST_18_YEARS_OLD));

        assertEquals(3, trialEligibility.cohorts().size());
        for (CohortEligibility cohort : trialEligibility.cohorts()) {
            assertEquals(Evaluation.PASS, cohort.overallEvaluation());
            assertTrue(cohort.evaluations().isEmpty());
        }
    }

    @NotNull
    private static Evaluation find(@NotNull Map<Eligibility, Evaluation> evaluations, @NotNull EligibilityRule ruleToFind) {
        for (Map.Entry<Eligibility, Evaluation> evaluation : evaluations.entrySet()) {
            if (evaluation.getKey().function().rule() == ruleToFind) {
                return evaluation.getValue();
            }
        }

        throw new IllegalStateException("Cannot find evaluation for rule " + ruleToFind);
    }
}