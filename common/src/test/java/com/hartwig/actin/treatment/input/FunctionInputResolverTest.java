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
import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory;
import com.hartwig.actin.treatment.input.datamodel.ImmutableTreatmentInputWithName;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableOneHlaAllele;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegersManyStrings;
import com.hartwig.actin.treatment.input.single.ManyTreatmentsWithName;
import com.hartwig.actin.treatment.input.single.OneHlaAllele;
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
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertNotNull(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        }
    }

    @Test
    public void canResolveCompositeInputs() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        // No inputs
        List<Object> inputs = Lists.newArrayList();
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Add first input
        inputs.add(createValidTestFunction());
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.AND, inputs)));
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.OR, inputs)));

        EligibilityFunction valid1 = create(EligibilityRule.NOT, inputs);
        assertTrue(resolver.hasValidInputs(valid1));
        assertNotNull(FunctionInputResolver.createOneCompositeParameter(valid1));
        assertTrue(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Add 2nd input
        inputs.add(createValidTestFunction());
        EligibilityFunction valid2 = create(EligibilityRule.OR, inputs);
        assertTrue(resolver.hasValidInputs(valid2));
        assertNotNull(FunctionInputResolver.createAtLeastTwoCompositeParameters(valid2));
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.NOT, inputs)));

        // Add 3rd input
        inputs.add(createValidTestFunction());
        assertTrue(resolver.hasValidInputs(create(EligibilityRule.OR, inputs)));
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.WARN_IF, inputs)));

        // Make sure that the check fails when number of inputs is correct but datamodel is not.
        assertFalse(resolver.hasValidInputs(create(EligibilityRule.AND, Lists.newArrayList("not a function", "not a function either"))));
    }

    @Test
    public void canResolveFunctionsWithoutInputs() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.NONE);

        assertTrue(resolver.hasValidInputs(create(rule, Lists.newArrayList())));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1 is too many"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(2, resolver.createOneIntegerInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1", "2"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithTwoIntegerInputs() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.TWO_INTEGERS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "3"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoIntegers.builder().integer1(2).integer2(3).build(), resolver.createTwoIntegersInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "also not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoubleInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_DOUBLE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(3.1, resolver.createOneDoubleInput(valid), EPSILON);

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "3.2"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a double"))));
    }

    @Test
    public void canResolveFunctionsWithTwoDoubleInputs() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.TWO_DOUBLES);

        EligibilityFunction valid = create(rule, Lists.newArrayList("3.1", "3.2"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoDoubles.builder().double1(3.1).double2(3.2).build(), resolver.createTwoDoublesInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("3.1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT);

        String treatment = TreatmentInput.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(treatment));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(TreatmentInput.IMMUNOTHERAPY, resolver.createOneTreatmentInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment input"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentOneIntegerInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_ONE_INTEGER);

        String treatment = TreatmentInput.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(treatment, "1"));
        assertTrue(resolver.hasValidInputs(valid));

        OneTreatmentOneInteger inputs = resolver.createOneTreatmentOneIntegerInput(valid);
        assertEquals(TreatmentInput.IMMUNOTHERAPY, inputs.treatment());
        assertEquals(1, inputs.integer());

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment input", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTypedTreatmentManyStringsInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS);

        String category = TreatmentCategory.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "string1;string2"));
        assertTrue(resolver.hasValidInputs(valid));

        OneTypedTreatmentManyStrings inputs = resolver.createOneTypedTreatmentManyStringsInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("string1"));
        assertTrue(inputs.strings().contains("string2"));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList(category))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentManyStringsOneIntegerInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TYPED_TREATMENT_MANY_STRINGS_ONE_INTEGER);

        String category = TreatmentCategory.IMMUNOTHERAPY.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "hello1; hello2", "1"));
        assertTrue(resolver.hasValidInputs(valid));

        OneTypedTreatmentManyStringsOneInteger inputs = resolver.createOneTypedTreatmentManyStringsOneIntegerInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.category());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("hello1"));
        assertTrue(inputs.strings().contains("hello2"));
        assertEquals(1, inputs.integer());

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList(TreatmentCategory.ANTIVIRAL_THERAPY.display(), "test", "1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList(category, "1", "hello1;hello2"))));
    }

    @Test
    public void canResolveFunctionsWithOneTumorTypeInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TUMOR_TYPE);

        String category = TumorTypeInput.CARCINOMA.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(TumorTypeInput.CARCINOMA, resolver.createOneTumorTypeInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a tumor type"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("0045"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals("0045", resolver.createOneStringInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("012", "234"))));
    }

    @Test
    public void canResolveFunctionsWithOneStringOneIntegerInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING_ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("string", "1"));
        assertTrue(resolver.hasValidInputs(valid));
        OneIntegerOneString inputs = resolver.createOneStringOneIntegerInput(valid);
        assertEquals("string", inputs.string());
        assertEquals(1, inputs.integer());

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1", "string"))));
    }

    @Test
    public void canResolveFunctionsWithManyStringsOneIntegerInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.MANY_STRINGS_ONE_INTEGER);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF;KRAS", "1"));
        assertTrue(resolver.hasValidInputs(valid));
        OneIntegerManyStrings expected =
                ImmutableOneIntegerManyStrings.builder().integer(1).strings(Lists.newArrayList("BRAF", "KRAS")).build();
        assertEquals(expected, resolver.createManyStringsOneIntegerInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1", "BRAF;KRAS"))));
    }

    @Test
    public void canResolveFunctionsWithManyStringsTwoIntegersInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.MANY_STRINGS_TWO_INTEGERS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF;KRAS", "1", "2"));
        assertTrue(resolver.hasValidInputs(valid));
        TwoIntegersManyStrings expected =
                ImmutableTwoIntegersManyStrings.builder().integer1(1).integer2(2).strings(Lists.newArrayList("BRAF", "KRAS")).build();
        assertEquals(expected, resolver.createManyStringsTwoIntegersInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1", "BRAF;KRAS"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("BRAF;KRAS", "1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("BRAF;KRAS", "1", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithManyTreatmentsWithNameInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.MANY_TREATMENTS_WITH_NAME);

        EligibilityFunction valid = create(rule,
                Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display() + "; name1", TreatmentInput.ANTIVIRAL_THERAPY.display()));
        assertTrue(resolver.hasValidInputs(valid));

        ManyTreatmentsWithName output = resolver.createManyTreatmentsWithNames(valid);
        assertEquals(2, output.treatmentsWithName().size());
        assertTrue(output.treatmentsWithName()
                .contains(ImmutableTreatmentInputWithName.builder().treatment(TreatmentInput.IMMUNOTHERAPY).name("name1").build()));
        assertTrue(output.treatmentsWithName()
                .contains(ImmutableTreatmentInputWithName.builder().treatment(TreatmentInput.ANTIVIRAL_THERAPY).name(null).build()));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment;"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display() + ";name1;name2"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerOneStringInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_ONE_STRING);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test"));
        assertTrue(resolver.hasValidInputs(valid));
        assertEquals(ImmutableOneIntegerOneString.builder().integer(2).string("test").build(),
                resolver.createOneIntegerOneStringInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneIntegerManyStringsInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_INTEGER_MANY_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("2", "test1;test2;test3"));
        assertTrue(resolver.hasValidInputs(valid));
        OneIntegerManyStrings expected =
                ImmutableOneIntegerManyStrings.builder().integer(2).strings(Lists.newArrayList("test1", "test2", "test3")).build();

        assertEquals(expected, resolver.createOneIntegerManyStringsInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("not an integer", "not an integer"))));
    }

    @Test
    public void canResolveFunctionsWithOneTumorStageInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_TUMOR_STAGE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("IIIA"));
        assertTrue(resolver.hasValidInputs(valid));

        assertEquals(TumorStage.IIIA, resolver.createOneTumorStageInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("IIIa"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("II", "III"))));
    }

    @Test
    public void canResolveFunctionsWithOneHlaAlleleInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createTestResolver();

        EligibilityRule rule = firstOfType(FunctionInput.ONE_HLA_ALLELE);

        EligibilityFunction valid = create(rule, Lists.newArrayList("A*02:01"));
        assertTrue(resolver.hasValidInputs(valid));

        OneHlaAllele expected = ImmutableOneHlaAllele.builder().allele("A*02:01").build();
        assertEquals(expected, resolver.createOneHlaAlleleInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("HLA-A*02:01"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("A*02"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("A:01*02"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("A*02:01", "A*02:02"))));
    }

    @Test
    public void canResolveFunctionsWithOneDoidTermInput() {
        FunctionInputResolver resolver = TestFunctionInputResolveFactory.createResolverWithDoidAndTerm("doid 1", "term 1");

        EligibilityRule rule = firstOfType(FunctionInput.ONE_DOID_TERM);

        EligibilityFunction valid = create(rule, Lists.newArrayList("term 1"));
        assertTrue(resolver.hasValidInputs(valid));

        assertEquals("term 1", resolver.createOneDoidTermInput(valid));

        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("doid 1"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("term 2"))));
        assertFalse(resolver.hasValidInputs(create(rule, Lists.newArrayList("term 1", "term 2"))));
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