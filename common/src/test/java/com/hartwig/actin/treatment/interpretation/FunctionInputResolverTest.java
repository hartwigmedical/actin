package com.hartwig.actin.treatment.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;
import com.hartwig.actin.treatment.interpretation.single.FunctionInput;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneIntegerManyStringsInput;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneIntegerOneStringInput;
import com.hartwig.actin.treatment.interpretation.single.ImmutableTwoStringInput;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerManyStringsInput;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneString;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class FunctionInputResolverTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void everyRuleHasConfiguredExpectedInputs() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            if (!CompositeRules.isComposite(rule)) {
                assertTrue(FunctionInputResolver.RULE_INPUT_MAP.containsKey(rule));
            }
        }
    }

    @Test
    public void canDetermineParameterValidityForEveryRule() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertNotNull(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        }
    }

    @Test
    public void canResolveCompositeParameters() {
        List<Object> inputs = Lists.newArrayList();

        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_ON_PASS, inputs)));

        // Add first param
        inputs.add(createValidTestFunction());
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.OR, inputs)));

        EligibilityFunction valid1 = create(EligibilityRule.NOT, inputs);
        assertTrue(FunctionInputResolver.hasValidInputs(valid1));
        assertNotNull(FunctionInputResolver.createOneCompositeParameter(valid1));
        assertTrue(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_ON_PASS, inputs)));

        // Add 2nd param
        inputs.add(createValidTestFunction());
        EligibilityFunction valid2 = create(EligibilityRule.OR, inputs);
        assertTrue(FunctionInputResolver.hasValidInputs(valid2));
        assertNotNull(FunctionInputResolver.createAtLeastTwoCompositeParameters(valid2));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.NOT, inputs)));

        // Add 3rd param
        inputs.add(createValidTestFunction());
        assertTrue(FunctionInputResolver.hasValidInputs(create(EligibilityRule.OR, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_ON_PASS, inputs)));

        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND,
                Lists.newArrayList("not a function", "not a function either"))));
    }

    @Test
    public void canResolveFunctionsWithoutParameters() {
        EligibilityRule rule = firstOfType(FunctionInput.NONE);

        assertTrue(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1 is too many"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(2, FunctionInputResolver.createOneIntegerInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "2"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoubleParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_DOUBLE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(3.1, FunctionInputResolver.createOneDoubleInput(valid), EPSILON);

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "3.2"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, FunctionInputResolver.createOneTreatmentCategoryInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryOneStringParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "string"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryOneString params = FunctionInputResolver.createOneTreatmentCategoryOneStringInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, params.treatmentCategory());
        assertEquals("string", params.string());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("0045"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals("0045", FunctionInputResolver.createOneStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("012", "234"))));
    }

    @Test
    public void canResolveFunctionsWithTwoStringParameters() {
        EligibilityRule rule = firstOfType(FunctionInput.TWO_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF", "V600E"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoStringInput.builder().string1("BRAF").string2("V600E").build(),
                FunctionInputResolver.createTwoStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("012"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerOneStringParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableOneIntegerOneStringInput.builder().integer(2).string("test").build(),
                FunctionInputResolver.createOneIntegerOneStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerManyStringsParameter() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_MANY_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test1;test2;test3"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        OneIntegerManyStringsInput expected =
                ImmutableOneIntegerManyStringsInput.builder().integer(2).strings(Lists.newArrayList("test1", "test2", "test3")).build();

        assertEquals(expected, FunctionInputResolver.createOneIntegerManyStringsInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @NotNull
    private static EligibilityRule firstOfType(@NotNull FunctionInput input) {
        for (Map.Entry<EligibilityRule, FunctionInput> entry : FunctionInputResolver.RULE_INPUT_MAP.entrySet()) {
            if (entry.getValue() == input) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Could not find single rule requiring input: " + input);

    }

    @NotNull
    private static EligibilityFunction createValidTestFunction() {
        return create(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, Lists.newArrayList());
    }

    @NotNull
    private static EligibilityFunction create(@NotNull EligibilityRule rule, @NotNull List<Object> parameters) {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build();
    }
}