package com.hartwig.actin.treatment.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegersManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableTwoStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class FunctionInputResolverTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canDetermineInputValidityForEveryRule() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertNotNull(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        }
    }

    @Test
    public void canResolveCompositeInputs() {
        List<Object> inputs = Lists.newArrayList();

        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Add first input
        inputs.add(createValidTestFunction());
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.OR, inputs)));

        EligibilityFunction valid1 = create(EligibilityRule.NOT, inputs);
        assertTrue(FunctionInputResolver.hasValidInputs(valid1));
        assertNotNull(FunctionInputResolver.createOneCompositeParameter(valid1));
        assertTrue(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Add 2nd input
        inputs.add(createValidTestFunction());
        EligibilityFunction valid2 = create(EligibilityRule.OR, inputs);
        assertTrue(FunctionInputResolver.hasValidInputs(valid2));
        assertNotNull(FunctionInputResolver.createAtLeastTwoCompositeParameters(valid2));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.NOT, inputs)));

        // Add 3rd input
        inputs.add(createValidTestFunction());
        assertTrue(FunctionInputResolver.hasValidInputs(create(EligibilityRule.OR, inputs)));
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Make sure that the check fails when number of inputs is correct but datamodel is not.
        assertFalse(FunctionInputResolver.hasValidInputs(create(EligibilityRule.AND,
                Lists.newArrayList("not a function", "not a function either"))));
    }

    @Test
    public void canResolveFunctionsWithoutInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.NONE);

        assertTrue(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1 is too many"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(2, FunctionInputResolver.createOneIntegerInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "2"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithTwoIntegerInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.TWO_INTEGERS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "3"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoIntegers.builder().integer1(2).integer2(3).build(), FunctionInputResolver.createTwoIntegersInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "also not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoubleInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_DOUBLE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(3.1, FunctionInputResolver.createOneDoubleInput(valid), EPSILON);

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "3.2"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a double"))));
    }

    @Test
    public void canResolveFunctionsWithTwoDoubleInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.TWO_DOUBLES);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1", "3.2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoDoubles.builder().double1(3.1).double2(3.2).build(), FunctionInputResolver.createTwoDoublesInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT);

        String treatment = TreatmentInput.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(treatment));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(TreatmentInput.IMMUNOTHERAPY, FunctionInputResolver.createOneTreatmentInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment input"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_ONE_INTEGER);

        String treatment = TreatmentInput.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(treatment, "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentOneInteger inputs = FunctionInputResolver.createOneTreatmentOneIntegerInput(valid);
        assertEquals(TreatmentInput.IMMUNOTHERAPY, inputs.treatment());
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment input", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTypedTreatmentManyStringsInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS);

        String category = TreatmentCategory.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "string1;string2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTypedTreatmentManyStrings inputs = FunctionInputResolver.createOneTypedTreatmentManyStringsInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("string1"));
        assertTrue(inputs.strings().contains("string2"));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList(category))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule,
                Lists.newArrayList(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentManyStringsOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS_ONE_INTEGER);

        String category = TreatmentCategory.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "hello1; hello2", "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTypedTreatmentManyStringsOneInteger inputs = FunctionInputResolver.createOneTypedTreatmentManyStringsOneIntegerInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("hello1"));
        assertTrue(inputs.strings().contains("hello2"));
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule,
                Lists.newArrayList(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test", "1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList(category, "1", "hello1;hello2"))));
    }

    @Test
    public void canResolveFunctionsWithOneTumorTypeInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TUMOR_TYPE);

        String category = TumorTypeInput.CARCINOMA.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(TumorTypeInput.CARCINOMA, FunctionInputResolver.createOneTumorTypeInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a tumor type"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("0045"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals("0045", FunctionInputResolver.createOneStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("012", "234"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING_ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("doid", "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        OneIntegerOneString inputs = FunctionInputResolver.createOneStringOneIntegerInput(valid);
        assertEquals("doid", inputs.string());
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "doid"))));
    }

    @Test
    public void canResolveFunctionsWithTwoStringInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.TWO_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF", "V600E"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoStrings.builder().string1("BRAF").string2("V600E").build(),
                FunctionInputResolver.createTwoStringsInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("012"))));
    }

    @Test
    public void canResolveFunctionsWithManyStringsOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.MANY_STRINGS_ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF;KRAS", "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        OneIntegerManyStrings expected =
                ImmutableOneIntegerManyStrings.builder().integer(1).strings(Lists.newArrayList("BRAF", "KRAS")).build();
        assertEquals(expected, FunctionInputResolver.createManyStringsOneIntegerInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "BRAF;KRAS"))));
    }

    @Test
    public void canResolveFunctionsWithManyStringsTwoIntegersInput() {
        EligibilityRule rule = firstOfType(FunctionInput.MANY_STRINGS_TWO_INTEGERS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF;KRAS", "1", "2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        TwoIntegersManyStrings expected =
                ImmutableTwoIntegersManyStrings.builder().integer1(1).integer2(2).strings(Lists.newArrayList("BRAF", "KRAS")).build();
        assertEquals(expected, FunctionInputResolver.createManyStringsTwoIntegersInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "BRAF;KRAS"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("BRAF;KRAS", "1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("BRAF;KRAS", "1", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerOneStringInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableOneIntegerOneString.builder().integer(2).string("test").build(),
                FunctionInputResolver.createOneIntegerOneStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerManyStringsInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_MANY_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test1;test2;test3"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        OneIntegerManyStrings expected =
                ImmutableOneIntegerManyStrings.builder().integer(2).strings(Lists.newArrayList("test1", "test2", "test3")).build();

        assertEquals(expected, FunctionInputResolver.createOneIntegerManyStringsInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneTumorStageInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TUMOR_STAGE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("IIIA"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        assertEquals(TumorStage.IIIA, FunctionInputResolver.createOneTumorStageInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("IIIa"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("II", "III"))));
    }

    @NotNull
    private static EligibilityRule firstOfType(@NotNull FunctionInput input) {
        for (Map.Entry<EligibilityRule, FunctionInput> entry : FunctionInputMapping.RULE_INPUT_MAP.entrySet()) {
            if (entry.getValue() == input) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("Could not find single rule requiring input: " + input);
    }

    @NotNull
    private static EligibilityFunction createValidTestFunction() {
        return create(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, Lists.newArrayList("18"));
    }

    @NotNull
    private static EligibilityFunction create(@NotNull EligibilityRule rule, @NotNull List<Object> parameters) {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build();
    }
}