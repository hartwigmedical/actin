package com.hartwig.actin.algo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialMatcherTest {

    @Test
    public void canMatchTrialsOnProperTestData() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();
        Trial trial = TestTreatmentFactory.createProperTestTrial();

        TrialMatcher matcher = new TrialMatcher(createTestEvaluationFunctionFactory());
        TreatmentMatch match = matcher.determineEligibility(patient, Lists.newArrayList(trial));

        assertEquals(match.sampleId(), patient.sampleId());
        assertEquals(1, match.trialMatches().size());

        assertTrialMatch(match.trialMatches().get(0));
    }

    @NotNull
    private static EvaluationFunctionFactory createTestEvaluationFunctionFactory() {
        return EvaluationFunctionFactory.withDoidModel(TestDoidModelFactory.createMinimalTestDoidModel());
    }

    private static void assertTrialMatch(@NotNull TrialEligibility trialEligibility) {
        assertEquals(1, trialEligibility.evaluations().size());
        assertEquals(Evaluation.PASS, trialEligibility.overallEvaluation());
        assertEquals(Evaluation.PASS, findEvaluationForRule(trialEligibility.evaluations(), EligibilityRule.IS_AT_LEAST_18_YEARS_OLD));

        assertEquals(3, trialEligibility.cohorts().size());

        CohortEligibility cohortA = findCohort(trialEligibility.cohorts(), "A");
        assertEquals(1, cohortA.evaluations().size());
        assertEquals(Evaluation.FAIL, cohortA.overallEvaluation());
        assertEquals(Evaluation.FAIL, findEvaluationForRule(cohortA.evaluations(), EligibilityRule.NOT));

        CohortEligibility cohortB = findCohort(trialEligibility.cohorts(), "B");
        assertEquals(Evaluation.PASS, cohortB.overallEvaluation());
        assertEquals(Evaluation.IGNORED, findEvaluationForRule(cohortB.evaluations(), EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS));

        CohortEligibility cohortC = findCohort(trialEligibility.cohorts(), "C");
        assertEquals(Evaluation.PASS, cohortC.overallEvaluation());
        assertTrue(cohortC.evaluations().isEmpty());
    }

    @NotNull
    private static Evaluation findEvaluationForRule(@NotNull Map<Eligibility, Evaluation> evaluations,
            @NotNull EligibilityRule ruleToFind) {
        for (Map.Entry<Eligibility, Evaluation> evaluation : evaluations.entrySet()) {
            if (evaluation.getKey().function().rule() == ruleToFind) {
                return evaluation.getValue();
            }
        }

        throw new IllegalStateException("Cannot find evaluation for rule '" + ruleToFind + "'");
    }

    @NotNull
    private static CohortEligibility findCohort(@NotNull List<CohortEligibility> cohorts, @NotNull String cohortIdToFind) {
        for (CohortEligibility cohort : cohorts) {
            if (cohort.metadata().cohortId().equals(cohortIdToFind)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Cannot find cohort with id '" + cohortIdToFind + "'");
    }
}