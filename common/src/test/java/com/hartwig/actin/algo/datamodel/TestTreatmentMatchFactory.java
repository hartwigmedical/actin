package com.hartwig.actin.algo.datamodel;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.jetbrains.annotations.NotNull;

public final class TestTreatmentMatchFactory {

    private TestTreatmentMatchFactory() {
    }

    @NotNull
    public static TreatmentMatch createMinimalTreatmentMatch() {
        return ImmutableTreatmentMatch.builder().sampleId(TestDataFactory.TEST_SAMPLE).build();
    }

    @NotNull
    public static TreatmentMatch createProperTreatmentMatch() {
        return ImmutableTreatmentMatch.builder().from(createMinimalTreatmentMatch()).trialMatches(createTestTrialMatches()).build();
    }

    @NotNull
    private static List<TrialEligibility> createTestTrialMatches() {
        List<TrialEligibility> matches = Lists.newArrayList();

        matches.add(ImmutableTrialEligibility.builder()
                .trialId("test trial")
                .overallEvaluation(Evaluation.PASS)
                .evaluations(createTestGeneralEvaluations())
                .cohorts(createTestCohorts())
                .build());

        return matches;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestGeneralEvaluations() {
        Map<Eligibility, Evaluation> map = Maps.newHashMap();

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Is adult").build())
                .build(), Evaluation.PASS);

        return map;
    }

    @NotNull
    private static List<CohortEligibility> createTestCohorts() {
        List<CohortEligibility> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohortEligibility.builder()
                .cohortId("A")
                .overallEvaluation(Evaluation.UNDETERMINED)
                .evaluations(createTestCohortEvaluations())
                .build());

        cohorts.add(ImmutableCohortEligibility.builder().cohortId("B").overallEvaluation(Evaluation.PASS).build());
        cohorts.add(ImmutableCohortEligibility.builder().cohortId("C").overallEvaluation(Evaluation.PASS).build());

        return cohorts;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestCohortEvaluations() {
        Map<Eligibility, Evaluation> map = Maps.newHashMap();

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_CNS_METASTASES).build())
                        .build())
                .addReferences(ImmutableCriterionReference.builder().id("I-02").text("Has no active CNS metastases").build())
                .build(), Evaluation.PASS);

        return map;
    }
}
