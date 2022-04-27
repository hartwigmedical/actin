package com.hartwig.actin.algo.datamodel;

import java.time.LocalDate;
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
        return ImmutableTreatmentMatch.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .referenceDate(LocalDate.of(2021, 8, 2))
                .referenceDateIsLive(true)
                .build();
    }

    @NotNull
    public static TreatmentMatch createProperTreatmentMatch() {
        return ImmutableTreatmentMatch.builder().from(createMinimalTreatmentMatch()).trialMatches(createTestTrialMatches()).build();
    }

    @NotNull
    private static List<TrialMatch> createTestTrialMatches() {
        List<TrialMatch> matches = Lists.newArrayList();

        matches.add(ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 1")
                        .acronym("TEST-TRIAL-1")
                        .title("This is the first ACTIN test trial")
                        .build())
                .isPotentiallyEligible(true)
                .evaluations(createTestGeneralEvaluationsTrial1())
                .cohorts(createTestCohorts())
                .build());

        matches.add(ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 2")
                        .acronym("TEST-TRIAL-2")
                        .title("This is the second ACTIN test trial")
                        .build())
                .isPotentiallyEligible(false)
                .evaluations(createTestGeneralEvaluationsTrial2())
                .build());

        return matches;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestGeneralEvaluationsTrial1() {
        Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Is adult").build())
                .build(), TestEvaluationFactory.withResult(EvaluationResult.PASS));

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES)
                                .build())
                        .build())
                .addReferences(ImmutableCriterionReference.builder()
                        .id("E-01")
                        .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                        .build())
                .build(), TestEvaluationFactory.withResult(EvaluationResult.PASS));

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .addReferences(ImmutableCriterionReference.builder()
                        .id("E-01")
                        .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                        .build())
                .build(), TestEvaluationFactory.withResult(EvaluationResult.NOT_EVALUATED));

        return map;
    }

    @NotNull
    private static List<CohortMatch> createTestCohorts() {
        List<CohortMatch> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, true, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluations())
                .build());
        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(true)
                .build());
        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("C", true, true, true))
                .isPotentiallyEligible(false)
                .build());

        return cohorts;
    }

    @NotNull
    private static CohortMetadata createTestMetadata(@NotNull String cohortId, boolean open, boolean slotsAvailable, boolean blacklist) {
        return ImmutableCohortMetadata.builder()
                .cohortId(cohortId)
                .open(open)
                .slotsAvailable(slotsAvailable)
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
                .build(), TestEvaluationFactory.withResult(EvaluationResult.FAIL));

        return map;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestGeneralEvaluationsTrial2() {
        Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_INFECTION).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Should have active infection").build())
                .build(), TestEvaluationFactory.withResult(EvaluationResult.FAIL));

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-02").text("Should be able to give consent").build())
                .build(), TestEvaluationFactory.withResult(EvaluationResult.WARN));

        return map;
    }
}
