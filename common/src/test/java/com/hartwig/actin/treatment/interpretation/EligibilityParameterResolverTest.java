package com.hartwig.actin.treatment.interpretation;

import static org.junit.Assert.assertEquals;
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

    private static final double EPSILON = 1.0E-10;

    @Test
    public void everyRuleIsOfSingleType() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            int count = 0;
            if (CompositeRules.isComposite(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITH_ONE_DOUBLE_PARAMETER.contains(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITH_ONE_INTEGER_PARAMETER.contains(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER.contains(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITH_ONE_STRING_PARAMETER.contains(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITH_TWO_STRING_PARAMETERS.contains(rule)) {
                count++;
            }

            if (EligibilityParameterResolver.RULES_WITHOUT_PARAMETERS.contains(rule)) {
                count++;
            }

            assertEquals(1, count);
        }
    }

    @Test
    public void canDetermineParameterValidityForEveryRule() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertNotNull(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        }
    }

    @Test
    public void canResolveCompositeParameters() {
        List<Object> inputs = Lists.newArrayList();

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.WARN_ON_PASS, inputs)));

        // Add first param
        inputs.add(createValidTestFunction());
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND, inputs)));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.OR, inputs)));

        EligibilityFunction valid1 = create(EligibilityRule.NOT, inputs);
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid1));
        assertNotNull(EligibilityParameterResolver.createOneCompositeParameter(valid1));
        assertTrue(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.WARN_ON_PASS, inputs)));

        // Add 2nd param
        inputs.add(createValidTestFunction());
        EligibilityFunction valid2 = create(EligibilityRule.OR, inputs);
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid2));
        assertNotNull(EligibilityParameterResolver.createAtLeastTwoCompositeParameters(valid2));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.NOT, inputs)));

        // Add 3rd param
        inputs.add(createValidTestFunction());
        assertTrue(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.OR, inputs)));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.WARN_ON_PASS, inputs)));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(EligibilityRule.AND,
                Lists.newArrayList("not a function", "not a function either"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoubleParameter() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITH_ONE_DOUBLE_PARAMETER.iterator().next();

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(3.1, EligibilityParameterResolver.createOneDoubleParameter(valid), EPSILON);

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("3.1", "3.2"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerParameter() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITH_ONE_INTEGER_PARAMETER.iterator().next();

        EligibilityFunction valid = create(rule, Lists.newArrayList("2"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(2, EligibilityParameterResolver.createOneIntegerParameter(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1", "2"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerOneStringParameter() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER.iterator().next();

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(Lists.newArrayList(2, "test"), EligibilityParameterResolver.createOneIntegerOneStringParameter(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringParameter() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITH_ONE_STRING_PARAMETER.iterator().next();

        EligibilityFunction valid = create(rule, Lists.newArrayList("0045"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals("0045", EligibilityParameterResolver.createOneStringParameter(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("012", "234"))));
    }

    @Test
    public void canResolveFunctionsWithTwoStringParameters() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITH_TWO_STRING_PARAMETERS.iterator().next();

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF", "V600E"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(Lists.newArrayList("BRAF", "V600E"), EligibilityParameterResolver.createTwoStringParameters(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("012"))));
    }

    @Test
    public void canResolveFunctionsWithoutParameters() {
        EligibilityRule rule = EligibilityParameterResolver.RULES_WITHOUT_PARAMETERS.iterator().next();

        assertTrue(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1 is too many"))));
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