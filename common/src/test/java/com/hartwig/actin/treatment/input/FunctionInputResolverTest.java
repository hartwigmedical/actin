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
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableTwoStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneStringTwoIntegers;
import com.hartwig.actin.treatment.input.single.OneTreatmentCategoryManyStrings;
import com.hartwig.actin.treatment.input.single.OneTreatmentCategoryManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentCategoryOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentCategoryOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentCategoryOneStringOneInteger;
import com.hartwig.actin.treatment.input.single.TumorTypeCategory;

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
        assertEquals(ImmutableTwoIntegers.builder().integer1(2).integer2(3).build(), FunctionInputResolver.createTwoIntegerInput(valid));

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
        assertEquals(ImmutableTwoDoubles.builder().double1(3.1).double2(3.2).build(), FunctionInputResolver.createTwoDoubleInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1"))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("3.1", "not a double"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, FunctionInputResolver.createOneTreatmentCategoryInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryOneStringInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "string"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryOneString inputs = FunctionInputResolver.createOneTreatmentCategoryOneStringInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatmentCategory());
        assertEquals("string", inputs.string());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryManyStringsInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_STRINGS);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "string1;string2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryManyStrings inputs = FunctionInputResolver.createOneTreatmentCategoryManyStringsInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatmentCategory());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("string1"));
        assertTrue(inputs.strings().contains("string2"));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList(category))));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_ONE_INTEGER);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryOneInteger inputs = FunctionInputResolver.createOneTreatmentCategoryOneIntegerInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatmentCategory());
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a treatment category", "test"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryOneStringOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "hello", "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryOneStringOneInteger inputs = FunctionInputResolver.createOneTreatmentCategoryOneStringOneIntegerInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatmentCategory());
        assertEquals("hello", inputs.string());
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList(category, "1", "hello"))));
    }

    @Test
    public void canResolveFunctionsWithOneTreatmentCategoryManyStringsOneIntegerInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_STRINGS_ONE_INTEGER);

        String category = TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY);
        EligibilityFunction valid = create(rule, Lists.newArrayList(category, "hello1; hello2", "1"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));

        OneTreatmentCategoryManyStringsOneInteger inputs =
                FunctionInputResolver.createOneTreatmentCategoryManyStringsOneIntegerInput(valid);
        assertEquals(TreatmentCategory.IMMUNOTHERAPY, inputs.treatmentCategory());
        assertEquals(2, inputs.strings().size());
        assertTrue(inputs.strings().contains("hello1"));
        assertTrue(inputs.strings().contains("hello2"));
        assertEquals(1, inputs.integer());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList(category, "1", "hello1;hello2"))));
    }

    @Test
    public void canResolveFunctionsWithOneTumorTypeCategoryInput() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_TUMOR_TYPE_CATEGORY);

        String category = TumorTypeCategory.CARCINOMA.display();
        EligibilityFunction valid = create(rule, Lists.newArrayList(category));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(TumorTypeCategory.CARCINOMA, FunctionInputResolver.createOneTumorTypeCategoryInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("not a tumor type category"))));
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
    public void canResolveFunctionsWithOneStringTwoIntegerInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.ONE_STRING_TWO_INTEGERS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("doid", "1", "2"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        OneStringTwoIntegers inputs = FunctionInputResolver.createOneStringTwoIntegerInput(valid);
        assertEquals("doid", inputs.string());
        assertEquals(1, inputs.integer1());
        assertEquals(2, inputs.integer2());

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("1", "2", "doid"))));
    }

    @Test
    public void canResolveFunctionsWithTwoStringInputs() {
        EligibilityRule rule = firstOfType(FunctionInput.TWO_STRINGS);

        EligibilityFunction valid = create(rule, Lists.newArrayList("BRAF", "V600E"));
        assertTrue(FunctionInputResolver.hasValidInputs(valid));
        assertEquals(ImmutableTwoStrings.builder().string1("BRAF").string2("V600E").build(),
                FunctionInputResolver.createTwoStringInput(valid));

        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList())));
        assertFalse(FunctionInputResolver.hasValidInputs(create(rule, Lists.newArrayList("012"))));
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