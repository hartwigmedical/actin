package com.hartwig.actin.treatment.input;

import static com.hartwig.actin.treatment.input.FunctionInputMapping.RULE_INPUT_MAP;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.input.single.ImmutableOneStringTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentOneString;
import com.hartwig.actin.treatment.input.single.ImmutableOneTreatmentOneStringOneInteger;
import com.hartwig.actin.treatment.input.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.input.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.input.single.ImmutableTwoStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneStringTwoIntegers;
import com.hartwig.actin.treatment.input.single.OneTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneStringOneInteger;
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
                case ONE_TREATMENT: {
                    createOneTreatmentInput(function);
                    return true;
                }
                case ONE_TREATMENT_ONE_STRING: {
                    createOneTreatmentOneStringInput(function);
                    return true;
                }
                case ONE_TREATMENT_MANY_STRINGS: {
                    createOneTreatmentManyStringsInput(function);
                    return true;
                }
                case ONE_TREATMENT_ONE_INTEGER: {
                    createOneTreatmentOneIntegerInput(function);
                    return true;
                }
                case ONE_TREATMENT_ONE_STRING_ONE_INTEGER: {
                    createOneTreatmentOneStringOneIntegerInput(function);
                    return true;
                }
                case ONE_TREATMENT_MANY_STRINGS_ONE_INTEGER: {
                    createOneTreatmentManyStringsOneIntegerInput(function);
                    return true;
                }
                case ONE_TUMOR_TYPE: {
                    createOneTumorTypeInput(function);
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
    public static TreatmentInput createOneTreatmentInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT, 1);

        return TreatmentInput.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public static OneTreatmentOneString createOneTreatmentOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_ONE_STRING, 2);

        return ImmutableOneTreatmentOneString.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneTreatmentManyStrings createOneTreatmentManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_MANY_STRINGS, 2);

        return ImmutableOneTreatmentManyStrings.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneTreatmentOneInteger createOneTreatmentOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_ONE_INTEGER, 2);

        return ImmutableOneTreatmentOneInteger.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneTreatmentOneStringOneInteger createOneTreatmentOneStringOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_ONE_STRING_ONE_INTEGER, 3);

        return ImmutableOneTreatmentOneStringOneInteger.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static OneTreatmentManyStringsOneInteger createOneTreatmentManyStringsOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_MANY_STRINGS_ONE_INTEGER, 3);

        return ImmutableOneTreatmentManyStringsOneInteger.builder()
                .treatment(TreatmentInput.fromString((String) function.parameters().get(0)))
                .strings(toStringList(function.parameters().get(1)))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static TumorTypeInput createOneTumorTypeInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TUMOR_TYPE, 1);

        return TumorTypeInput.fromString((String) function.parameters().get(0));
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
