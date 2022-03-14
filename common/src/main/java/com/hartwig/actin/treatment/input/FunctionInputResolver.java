package com.hartwig.actin.treatment.input;

import static com.hartwig.actin.treatment.input.FunctionInputMapping.RULE_INPUT_MAP;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeCategory;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableOneStringTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentCategoryManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentCategoryManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentCategoryOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentCategoryOneString;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentCategoryOneStringOneInteger;
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
import com.hartwig.actin.treatment.input.single.TwoDoubles;
import com.hartwig.actin.treatment.input.single.TwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoStrings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FunctionInputResolver {

    private static final Logger LOGGER = LogManager.getLogger(FunctionInputResolver.class);

    private static final String MANY_STRING_SEPARATOR = ";";

    private FunctionInputResolver() {
    }

    @Nullable
    public static Boolean hasValidInputs(@NotNull EligibilityFunction function) {
        if (CompositeRules.isComposite(function.rule())) {
            return hasValidCompositeInputs(function);
        } else {
            return hasValidSingleInputs(function);
        }
    }

    private static boolean hasValidCompositeInputs(@NotNull EligibilityFunction function) {
        try {
            CompositeInput requiredInputs = CompositeRules.inputsForCompositeRule(function.rule());
            if (requiredInputs == CompositeInput.AT_LEAST_2) {
                createAtLeastTwoCompositeParameters(function);
            } else if (requiredInputs == CompositeInput.EXACTLY_1) {
                createOneCompositeParameter(function);
            } else {
                throw new IllegalStateException(
                        "Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Nullable
    private static Boolean hasValidSingleInputs(@NotNull EligibilityFunction function) {
        try {
            switch (RULE_INPUT_MAP.get(function.rule())) {
                case NONE: {
                    return function.parameters().isEmpty();
                }
                case ONE_INTEGER: {
                    createOneIntegerInput(function);
                    return true;
                }
                case TWO_INTEGERS: {
                    createTwoIntegerInput(function);
                    return true;
                }
                case ONE_DOUBLE: {
                    createOneDoubleInput(function);
                    return true;
                }
                case TWO_DOUBLES: {
                    createTwoDoubleInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY: {
                    createOneTreatmentCategoryInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_STRING: {
                    createOneTreatmentCategoryOneStringInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_MANY_STRINGS: {
                    createOneTreatmentCategoryManyStringsInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_INTEGER: {
                    createOneTreatmentCategoryOneIntegerInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER: {
                    createOneTreatmentCategoryOneStringOneIntegerInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_MANY_STRINGS_ONE_INTEGER: {
                    createOneTreatmentCategoryManyStringsOneIntegerInput(function);
                    return true;
                }
                case ONE_TUMOR_TYPE_CATEGORY: {
                    createOneTumorTypeCategoryInput(function);
                    return true;
                }
                case ONE_STRING: {
                    createOneStringInput(function);
                    return true;
                }
                case ONE_STRING_ONE_INTEGER: {
                    createOneStringOneIntegerInput(function);
                    return true;
                }
                case ONE_STRING_TWO_INTEGERS: {
                    createOneStringTwoIntegerInput(function);
                    return true;
                }
                case TWO_STRINGS: {
                    createTwoStringInput(function);
                    return true;
                }
                case ONE_INTEGER_ONE_STRING: {
                    createOneIntegerOneStringInput(function);
                    return true;
                }
                case ONE_INTEGER_MANY_STRINGS: {
                    createOneIntegerManyStringsInput(function);
                    return true;
                }
                case ONE_TUMOR_STAGE: {
                    createOneTumorStageInput(function);
                    return true;
                }
                default: {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule());
                    return null;
                }
            }
        } catch (Exception exception) {
            return false;
        }
    }

    public static int createOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    @NotNull
    public static TwoIntegers createTwoIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_INTEGERS, 2);

        return ImmutableTwoIntegers.builder()
                .integer1(Integer.parseInt((String) function.parameters().get(0)))
                .integer2(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    public static double createOneDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    @NotNull
    public static TwoDoubles createTwoDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_DOUBLES, 2);

        return ImmutableTwoDoubles.builder()
                .double1(Double.parseDouble((String) function.parameters().get(0)))
                .double2(Double.parseDouble((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static TreatmentCategory createOneTreatmentCategoryInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY, 1);

        return TreatmentCategoryResolver.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public static OneTreatmentCategoryOneString createOneTreatmentCategoryOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING, 2);

        return ImmutableOneTreatmentCategoryOneString.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryManyStrings createOneTreatmentCategoryManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_STRINGS, 2);

        return ImmutableOneTreatmentCategoryManyStrings.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryOneInteger createOneTreatmentCategoryOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_INTEGER, 2);

        return ImmutableOneTreatmentCategoryOneInteger.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryOneStringOneInteger createOneTreatmentCategoryOneStringOneIntegerInput(
            @NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER, 3);

        return ImmutableOneTreatmentCategoryOneStringOneInteger.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryManyStringsOneInteger createOneTreatmentCategoryManyStringsOneIntegerInput(
            @NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_MANY_STRINGS_ONE_INTEGER, 3);

        return ImmutableOneTreatmentCategoryManyStringsOneInteger.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static TumorTypeCategory createOneTumorTypeCategoryInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_TYPE_CATEGORY, 1);

        return TumorTypeCategory.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public static String createOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public static OneIntegerOneString createOneStringOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING_ONE_INTEGER, 2);

        return ImmutableOneIntegerOneString.builder()
                .string((String) function.parameters().get(0))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneStringTwoIntegers createOneStringTwoIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING_TWO_INTEGERS, 3);

        return ImmutableOneStringTwoIntegers.builder()
                .string((String) function.parameters().get(0))
                .integer1(Integer.parseInt((String) function.parameters().get(1)))
                .integer2(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static TwoStrings createTwoStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_STRINGS, 2);

        return ImmutableTwoStrings.builder()
                .string1((String) function.parameters().get(0))
                .string2((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerOneString createOneIntegerOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_ONE_STRING, 2);

        return ImmutableOneIntegerOneString.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerManyStrings createOneIntegerManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_MANY_STRINGS, 2);

        return ImmutableOneIntegerManyStrings.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static TumorStage createOneTumorStageInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_STAGE, 1);

        return TumorStage.valueOf((String) function.parameters().get(0));
    }

    @NotNull
    public static EligibilityFunction createOneCompositeParameter(@NotNull EligibilityFunction function) {
        assertParamCount(function, 1);

        return (EligibilityFunction) function.parameters().get(0);
    }

    @NotNull
    public static List<EligibilityFunction> createAtLeastTwoCompositeParameters(@NotNull EligibilityFunction function) {
        if (function.parameters().size() < 2) {
            throw new IllegalArgumentException(
                    "Not enough parameters passed into '" + function.rule() + "': " + function.parameters().size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : function.parameters()) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }

    @NotNull
    private static List<String> toStringList(@NotNull Object param) {
        List<String> strings = Lists.newArrayList();
        for (String input : ((String) param).split(MANY_STRING_SEPARATOR)) {
            strings.add(input.trim());
        }
        return strings;
    }

    private static void assertParamConfig(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput, int expectedCount) {
        assertParamType(function, requestedInput);
        assertParamCount(function, expectedCount);
    }

    private static void assertParamType(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput) {
        if (requestedInput != RULE_INPUT_MAP.get(function.rule())) {
            throw new IllegalStateException("Incorrect type of inputs requested for '" + function.rule() + "': " + requestedInput);
        }
    }

    private static void assertParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size());
        }
    }
}
