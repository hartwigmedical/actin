package com.hartwig.actin.algo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.calendar.TestReferenceDateProviderFactory;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TrialMatch;
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
        List<TrialMatch> matches = matcher.determineEligibility(patient, Lists.newArrayList(trial));

        assertEquals(1, matches.size());

        assertTrialMatch(matches.get(0));
    }

    @NotNull
    private static EvaluationFunctionFactory createTestEvaluationFunctionFactory() {
        return EvaluationFunctionFactory.create(TestDoidModelFactory.createMinimalTestDoidModel(),
                TestReferenceDateProviderFactory.createCurrentDate());
    }

    private static void assertTrialMatch(@NotNull TrialMatch trialMatch) {
        assertEquals(1, trialMatch.evaluations().size());
        assertTrue(trialMatch.isPotentiallyEligible());
        assertEquals(EvaluationResult.PASS,
                findEvaluationResultForRule(trialMatch.evaluations(), EligibilityRule.IS_AT_LEAST_X_YEARS_OLD));

        assertEquals(3, trialMatch.cohorts().size());

        CohortMatch cohortA = findCohort(trialMatch.cohorts(), "A");
        assertEquals(1, cohortA.evaluations().size());
        assertFalse(cohortA.isPotentiallyEligible());
        assertEquals(EvaluationResult.FAIL, findEvaluationResultForRule(cohortA.evaluations(), EligibilityRule.NOT));

        CohortMatch cohortB = findCohort(trialMatch.cohorts(), "B");
        assertTrue(cohortB.isPotentiallyEligible());
        assertEquals(EvaluationResult.UNDETERMINED,
                findEvaluationResultForRule(cohortB.evaluations(), EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS));

        CohortMatch cohortC = findCohort(trialMatch.cohorts(), "C");
        assertTrue(cohortC.isPotentiallyEligible());
        assertTrue(cohortC.evaluations().isEmpty());
    }

    @NotNull
    private static EvaluationResult findEvaluationResultForRule(@NotNull Map<Eligibility, Evaluation> evaluations,
            @NotNull EligibilityRule ruleToFind) {
        for (Map.Entry<Eligibility, Evaluation> evaluation : evaluations.entrySet()) {
            if (evaluation.getKey().function().rule() == ruleToFind) {
                return evaluation.getValue().result();
            }
        }

        throw new IllegalStateException("Cannot find evaluation for rule '" + ruleToFind + "'");
    }

    @NotNull
    private static CohortMatch findCohort(@NotNull List<CohortMatch> cohorts, @NotNull String cohortIdToFind) {
        for (CohortMatch cohort : cohorts) {
            if (cohort.metadata().cohortId().equals(cohortIdToFind)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Cannot find cohort with id '" + cohortIdToFind + "'");
    }
}