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
        return ImmutableTrial.builder().trialId(TEST_TRIAL).acronym(Strings.EMPTY).title(Strings.EMPTY).build();
    }

    @NotNull
    public static Trial createProperTestTrial() {
        return ImmutableTrial.builder()
                .from(createMinimalTestTrial())
                .acronym("TEST-TRIAL")
                .title("This is an ACTIN test trial")
                .generalEligibilityFunctions(createGeneralEligibilityFunctions())
                .cohorts(createTestCohorts())
                .build();
    }

    @NotNull
    private static List<EligibilityFunction> createGeneralEligibilityFunctions() {
        List<EligibilityFunction> functions = Lists.newArrayList();

        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build());

        return functions;
    }

    @NotNull
    private static List<Cohort> createTestCohorts() {
        List<Cohort> cohorts = Lists.newArrayList();

        cohorts.add(ImmutableCohort.builder().cohortId("A").open(true).description("Cohort A").build());
        cohorts.add(ImmutableCohort.builder().cohortId("B").open(true).description("Cohort B").build());
        cohorts.add(ImmutableCohort.builder().cohortId("C").open(false).description("Cohort C").build());

        return cohorts;
    }
}
