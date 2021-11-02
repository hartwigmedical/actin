package com.hartwig.actin.treatment.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EligibilityParameterResolverTest {

    @Test
    public void canResolveCompositeParameters() {
        List<Object> inputs = Lists.newArrayList();

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));

        inputs.add(createValidTestFunction());
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));

        inputs.add(createValidTestFunction());
        assertTrue(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));
        assertNotNull(EligibilityParameterResolver.createCompositeParameters(inputs, 2));

        inputs.add(createValidTestFunction());
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND,
                Lists.newArrayList("not a function", "not a function either"))));
    }

    @Test
    public void canResolveFunctionsWithNoParameters() {
        assertTrue(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD,
                Lists.newArrayList("1 too many"))));
    }

    @NotNull
    private static EligibilityFunction createValidTestFunction() {
        return create(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, Lists.newArrayList());
    }

    @NotNull
    private static EligibilityFunction create(@NotNull EligibilityRule rule, @NotNull List<Object> parameters) {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build();
    }

}