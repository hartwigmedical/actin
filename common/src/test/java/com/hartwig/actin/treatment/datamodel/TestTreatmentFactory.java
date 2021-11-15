package com.hartwig.actin.treatment.datamodel;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTreatmentFactory {

    private static final String TEST_TRIAL = "test trial";

    private TestTreatmentFactory() {
    }

    @NotNull
    public static Trial createMinimalTestTrial() {
        return ImmutableTrial.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId(TEST_TRIAL)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build())
                .build();
    }

    @NotNull
    public static Trial createProperTestTrial() {
        Trial minimal = createMinimalTestTrial();
        return ImmutableTrial.builder()
                .from(minimal)
                .identification(ImmutableTrialIdentification.builder()
                        .from(minimal.identification())
                        .acronym("TEST-TRIAL")
                        .title("This is an ACTIN test trial")
                        .build())
                .generalEligibility(createGeneralEligibility())
                .cohorts(createTestCohorts())
                .build();
    }

    @NotNull
    private static List<Eligibility> createGeneralEligibility() {
        List<Eligibility> functions = Lists.newArrayList();

        functions.add(ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Is adult").build())
                .build());

        return functions;
    }

    @NotNull
    private static List<Cohort> createTestCohorts() {
        List<Cohort> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohort.builder()
                .metadata(createTestMetadata("A"))
                .addEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.NOT)
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.HAS_ACTIVE_CNS_METASTASES)
                                        .build())
                                .build())
                        .addReferences(ImmutableCriterionReference.builder().id("I-02").text("Has no active CNS metastases").build())
                        .build())
                .build());
        cohorts.add(ImmutableCohort.builder().metadata(createTestMetadata("B")).build());
        cohorts.add(ImmutableCohort.builder().metadata(createTestMetadata("C")).build());

        return cohorts;
    }

    @NotNull
    private static CohortMetadata createTestMetadata(@NotNull String cohortId) {
        return ImmutableCohortMetadata.builder().cohortId(cohortId).open(true).description("Cohort " + cohortId).build();
    }
}
