package com.hartwig.actin.algo.datamodel;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;
import com.hartwig.actin.treatment.sort.EligibilityComparator;

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
                .identification(ImmutableTrialIdentification.builder()
                        .trialId("Test Trial")
                        .acronym("TEST-TRIAL")
                        .title("This is an ACTIN test trial")
                        .build())
                .overallEvaluation(Evaluation.PASS)
                .evaluations(createTestGeneralEvaluations())
                .cohorts(createTestCohorts())
                .build());

        return matches;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestGeneralEvaluations() {
        Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Is adult").build())
                .build(), Evaluation.PASS);

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES)
                                .build())
                        .build())
                .addReferences(ImmutableCriterionReference.builder()
                        .id("E-01")
                        .text("This rule has 2 conditions:\n 1. Patient has no symptomatic CNS metastases.\n 2. Patient has exhausted SOC.")
                        .build())
                .build(), Evaluation.PASS);

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .addReferences(ImmutableCriterionReference.builder()
                        .id("E-01")
                        .text("This rule has 2 conditions:\n 1. Patient has no symptomatic CNS metastases.\n 2. Patient has exhausted SOC.")
                        .build())
                .build(), Evaluation.NOT_EVALUATED);

        return map;
    }

    @NotNull
    private static List<CohortEligibility> createTestCohorts() {
        List<CohortEligibility> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohortEligibility.builder()
                .metadata(createTestMetadata("A", true, false))
                .overallEvaluation(Evaluation.FAIL)
                .evaluations(createTestCohortEvaluations())
                .build());
        cohorts.add(ImmutableCohortEligibility.builder()
                .metadata(createTestMetadata("B", true, false))
                .overallEvaluation(Evaluation.PASS)
                .build());
        cohorts.add(ImmutableCohortEligibility.builder()
                .metadata(createTestMetadata("C", true, true))
                .overallEvaluation(Evaluation.PASS)
                .build());

        return cohorts;
    }

    @NotNull
    private static CohortMetadata createTestMetadata(@NotNull String cohortId, boolean open, boolean blacklist) {
        return ImmutableCohortMetadata.builder()
                .cohortId(cohortId)
                .open(open)
                .blacklist(blacklist)
                .description("Cohort " + cohortId)
                .build();
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestCohortEvaluations() {
        Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES).build())
                        .build())
                .addReferences(ImmutableCriterionReference.builder().id("I-02").text("Has no active CNS metastases").build())
                .build(), Evaluation.FAIL);

        return map;
    }
}
