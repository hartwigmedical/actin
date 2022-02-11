package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EligibilityEvaluatorTest {

    @Test
    public void canEvaluateEligibility() {
        TrialEligibility passNoCohorts = createTestTrialBuilder().overallEvaluation(Evaluation.PASS).build();
        assertTrue(EligibilityEvaluator.isEligibleTrial(passNoCohorts));

        TrialEligibility passWithPassCohort = createTestTrialBuilder().overallEvaluation(Evaluation.PASS)
                .addCohorts(createTestCohortBuilder(false).overallEvaluation(Evaluation.PASS).build())
                .build();
        assertTrue(EligibilityEvaluator.isEligibleTrial(passWithPassCohort));

        TrialEligibility passWithFailCohort = createTestTrialBuilder().overallEvaluation(Evaluation.PASS)
                .addCohorts(createTestCohortBuilder(false).overallEvaluation(Evaluation.FAIL).build())
                .build();
        assertFalse(EligibilityEvaluator.isEligibleTrial(passWithFailCohort));

        TrialEligibility passWithBlacklistCohort = createTestTrialBuilder().overallEvaluation(Evaluation.PASS)
                .addCohorts(createTestCohortBuilder(true).overallEvaluation(Evaluation.PASS).build())
                .build();
        assertFalse(EligibilityEvaluator.isEligibleTrial(passWithBlacklistCohort));

        TrialEligibility fail = createTestTrialBuilder().overallEvaluation(Evaluation.FAIL).build();
        assertFalse(EligibilityEvaluator.isEligibleTrial(fail));
    }

    @NotNull
    private static ImmutableTrialEligibility.Builder createTestTrialBuilder() {
        return ImmutableTrialEligibility.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId(Strings.EMPTY)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build());
    }

    @NotNull
    private static ImmutableCohortEligibility.Builder createTestCohortBuilder(boolean blacklist) {
        return ImmutableCohortEligibility.builder()
                .metadata(ImmutableCohortMetadata.builder()
                        .cohortId(Strings.EMPTY)
                        .open(true)
                        .blacklist(blacklist)
                        .description(Strings.EMPTY)
                        .build());
    }
}