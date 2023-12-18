package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction

class EligibilityFunctionComparatorTest {
    @org.junit.Test
    fun canSortEligibilityFunctions() {
        val functions: MutableList<EligibilityFunction> = com.google.common.collect.Lists.newArrayList<EligibilityFunction>()
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
        functions.add(
            ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.NOT)
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .build()
        )
        functions.add(
            ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.NOT)
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .build()
        )
        functions.add(
            ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.AND)
                .addParameters(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .build()
                )
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_INFECTION).build())
                .build()
        )
        functions.add(
            ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.AND)
                .addParameters(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .build()
                )
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_INFECTION).build())
                .build()
        )
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X).addParameters("5").build())
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X).addParameters("6").build())
        functions.sort(EligibilityFunctionComparator())
        assertEquals(EligibilityRule.AND, functions[0].rule())
        assertEquals(EligibilityRule.AND, functions[1].rule())
        assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, functions[2].rule())
        assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, functions[3].rule())
        assertEquals(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, functions[4].rule())
        assertEquals(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, functions[5].rule())
        assertEquals(EligibilityRule.NOT, functions[6].rule())
        assertEquals(EligibilityRule.NOT, functions[7].rule())
    }
}