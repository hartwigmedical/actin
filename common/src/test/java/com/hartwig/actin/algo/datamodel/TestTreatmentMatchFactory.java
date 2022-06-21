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
import org.jetbrains.annotations.Nullable;

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
                        .open(true)
                        .acronym("TEST-1")
                        .title("Example test trial 1")
                        .build())
                .isPotentiallyEligible(true)
                .evaluations(createTestGeneralEvaluationsTrial1())
                .cohorts(createTestCohorts())
                .build());

        matches.add(ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 2")
                        .open(true)
                        .acronym("TEST-2")
                        .title("Example test trial 2")
                        .build())
                .isPotentiallyEligible(true)
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
                .build(), EvaluationTestFactory.withResult(EvaluationResult.PASS));

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES)
                                .build())
                        .build())
                .addReferences(ImmutableCriterionReference.builder()
                        .id("I-02")
                        .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                        .build())
                .build(), unrecoverable(EvaluationResult.PASS, "Patient has no known brain metastases", "No known brain metastases"));

        map.put(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .addReferences(ImmutableCriterionReference.builder()
                                .id("I-02")
                                .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                                .build())
                        .build(),
                unrecoverable(EvaluationResult.UNDETERMINED,
                        "Could not be determined if patient has exhausted SOC",
                        "Undetermined SOC exhaustion"));

        return map;
    }

    @NotNull
    private static List<CohortMatch> createTestCohorts() {
        List<CohortMatch> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, false, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluations())
                .build());
        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(true)
                .build());
        cohorts.add(ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("C", false, false, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluations())
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
                .addReferences(ImmutableCriterionReference.builder().id("E-01").text("Active CNS metastases").build())
                .build(), unrecoverable(EvaluationResult.FAIL, "Patient has active CNS metastases", "Active CNS metastases"));

        return map;
    }

    @NotNull
    private static Map<Eligibility, Evaluation> createTestGeneralEvaluationsTrial2() {
        Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());

        map.put(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_MEASURABLE_DISEASE).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Patient should have measurable disease").build())
                .build(), unrecoverable(EvaluationResult.PASS, "Patient has measurable disease"));

        map.put(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT).build())
                        .addReferences(ImmutableCriterionReference.builder()
                                .id("I-02")
                                .text("Patient should be able to give adequate informed consent")
                                .build())
                        .build(),
                unrecoverable(EvaluationResult.NOT_EVALUATED, "It is assumed that patient can provide adequate informed consent"));

        return map;
    }

    @NotNull
    private static Evaluation unrecoverable(@NotNull EvaluationResult result, @NotNull String specificMessage) {
        return unrecoverable(result, specificMessage, null);
    }

    @NotNull
    private static Evaluation unrecoverable(@NotNull EvaluationResult result, @NotNull String specificMessage,
            @Nullable String generalMessage) {
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result).recoverable(false);

        switch (result) {
            case PASS:
            case NOT_EVALUATED: {
                if (generalMessage != null) {
                    builder.addPassGeneralMessages(generalMessage);
                }
                builder.addPassSpecificMessages(specificMessage);
                break;
            }
            case WARN: {
                if (generalMessage != null) {
                    builder.addWarnGeneralMessages(generalMessage);
                }
                builder.addWarnSpecificMessages(specificMessage);
                break;
            }
            case FAIL: {
                if (generalMessage != null) {
                    builder.addFailGeneralMessages(generalMessage);
                }
                builder.addFailSpecificMessages(specificMessage);
                break;
            }
            case UNDETERMINED: {
                if (generalMessage != null) {
                    builder.addUndeterminedGeneralMessages(generalMessage);
                }
                builder.addUndeterminedSpecificMessages(specificMessage);
                break;
            }
        }

        return builder.build();
    }
}
