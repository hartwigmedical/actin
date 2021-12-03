package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.junit.Test;

public class EligibilityFunctionComparatorTest {

    @Test
    public void canSortEligibilityFunctions() {
        List<EligibilityFunction> functions = Lists.newArrayList();

        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build());
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build());
        functions.add(ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.NOT)
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .build());

        functions.add(ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.NOT)
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .build());

        functions.add(ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.AND)
                .addParameters(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .build())
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_INFECTION).build())
                .build());

        functions.add(ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.AND)
                .addParameters(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                        .build())
                .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_ACTIVE_INFECTION).build())
                .build());

        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X).addParameters("5").build());
        functions.add(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X).addParameters("6").build());

        functions.sort(new EligibilityFunctionComparator());

        assertEquals(EligibilityRule.AND, functions.get(0).rule());
        assertEquals(EligibilityRule.AND, functions.get(1).rule());
        assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, functions.get(2).rule());
        assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, functions.get(3).rule());
        assertEquals(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, functions.get(4).rule());
        assertEquals(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, functions.get(5).rule());
        assertEquals(EligibilityRule.NOT, functions.get(6).rule());
        assertEquals(EligibilityRule.NOT, functions.get(7).rule());
    }
}