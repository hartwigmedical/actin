package com.hartwig.actin.treatment.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EligibilityParameterResolverTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void everyRuleHasInputsConfigured() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            if (!CompositeRules.isComposite(rule)) {
                assertTrue(EligibilityParameterResolver.PARAMETER_MAP.containsKey(rule));
            }
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
    public void canResolveFunctionsWithoutParameters() {
        EligibilityRule rule = firstOfType(Inputs.NONE);

        assertTrue(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1 is too many"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerParameter() {
        EligibilityRule rule = firstOfType(Inputs.ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(2, EligibilityParameterResolver.createOneIntegerInput(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1", "2"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoubleParameter() {
        EligibilityRule rule = firstOfType(Inputs.ONE_DOUBLE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(3.1, EligibilityParameterResolver.createOneDoubleInput(valid), EPSILON);

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("3.1", "3.2"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringParameter() {
        EligibilityRule rule = firstOfType(Inputs.ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("0045"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals("0045", EligibilityParameterResolver.createOneStringInput(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("012", "234"))));
    }

    @Test
    public void canResolveFunctionsWithTwoStringParameters() {
        EligibilityRule rule = firstOfType(Inputs.TWO_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF", "V600E"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(ImmutableTwoStringInput.builder().string1("BRAF").string2("V600E").build(),
                EligibilityParameterResolver.createTwoStringInput(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("012"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerOneStringParameter() {
        EligibilityRule rule = firstOfType(Inputs.ONE_INTEGER_ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        assertEquals(ImmutableOneIntegerOneStringInput.builder().integer(2).string("test").build(),
                EligibilityParameterResolver.createOneIntegerOneStringInput(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerManyStringsParameter() {
        EligibilityRule rule = firstOfType(Inputs.ONE_INTEGER_MANY_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test1;test2;test3"));
        assertTrue(EligibilityParameterResolver.hasValidParameters(valid));
        OneIntegerManyStringsInput expected =
                ImmutableOneIntegerManyStringsInput.builder().integer(2).strings(Lists.newArrayList("test1", "test2", "test3")).build();

        assertEquals(expected, EligibilityParameterResolver.createOneIntegerManyStringsInput(valid));

        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList())));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("1"))));
        assertFalse(EligibilityParameterResolver.hasValidParameters(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @NotNull
    private static EligibilityRule firstOfType(@NotNull Inputs inputs) {
        for (Map.Entry<EligibilityRule, Inputs> entry : EligibilityParameterResolver.PARAMETER_MAP.entrySet()) {
            if (entry.getValue() == inputs) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Could not find single rule of type: " + inputs);

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