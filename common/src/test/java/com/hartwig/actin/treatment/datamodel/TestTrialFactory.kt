package com.hartwig.actin.treatment.datamodel

import com.google.common.collect.Lists
import org.apache.logging.log4j.util.Strings

object TestTrialFactory {
    private const val TEST_TRIAL = "test trial"
    fun createMinimalTestTrial(): Trial {
        return ImmutableTrial.builder()
            .identification(
                ImmutableTrialIdentification.builder()
                    .trialId(TEST_TRIAL)
                    .open(true)
                    .acronym(Strings.EMPTY)
                    .title(Strings.EMPTY)
                    .build()
            )
            .build()
    }

    fun createProperTestTrial(): Trial {
        val minimal = createMinimalTestTrial()
        return ImmutableTrial.builder()
            .from(minimal)
            .identification(
                ImmutableTrialIdentification.builder()
                    .from(minimal.identification())
                    .acronym("TEST-TRIAL")
                    .title("This is an ACTIN test trial")
                    .build()
            )
            .generalEligibility(createGeneralEligibility())
            .cohorts(createTestCohorts())
            .build()
    }

    private fun createGeneralEligibility(): List<Eligibility> {
        val functions: MutableList<Eligibility> = Lists.newArrayList()
        functions.add(
            ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).addParameters("18").build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Is adult").build())
                .build()
        )
        return functions
    }

    private fun createTestCohorts(): List<Cohort> {
        val cohorts: MutableList<Cohort> = Lists.newArrayList()
        cohorts.add(
            ImmutableCohort.builder()
                .metadata(createTestMetadata("A"))
                .addEligibility(
                    ImmutableEligibility.builder()
                        .function(
                            ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.NOT)
                                .addParameters(
                                    ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES)
                                        .build()
                                )
                                .build()
                        )
                        .addReferences(
                            ImmutableCriterionReference.builder()
                                .id("E-01")
                                .text("Has no active CNS metastases and has exhausted SOC")
                                .build()
                        )
                        .build()
                )
                .build()
        )
        cohorts.add(
            ImmutableCohort.builder()
                .metadata(createTestMetadata("B"))
                .addEligibility(
                    ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .addReferences(
                            ImmutableCriterionReference.builder()
                                .id("E-01")
                                .text("Has no active CNS metastases and has exhausted SOC")
                                .build()
                        )
                        .build()
                )
                .build()
        )
        cohorts.add(ImmutableCohort.builder().metadata(createTestMetadata("C")).build())
        cohorts.add(ImmutableCohort.builder().metadata(createTestMetadata("D", false)).build())
        return cohorts
    }

    private fun createTestMetadata(cohortId: String, evaluable: Boolean = true): CohortMetadata {
        return ImmutableCohortMetadata.builder()
            .cohortId(cohortId)
            .evaluable(evaluable)
            .open(true)
            .slotsAvailable(true)
            .blacklist(false)
            .description("Cohort $cohortId")
            .build()
    }
}
