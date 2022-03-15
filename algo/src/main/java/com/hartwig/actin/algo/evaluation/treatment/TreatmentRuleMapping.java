package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTreatmentManyStringsOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneStringOneInteger;

import org.jetbrains.annotations.NotNull;

public final class TreatmentRuleMapping {

    private TreatmentRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT, isEligibleForCurativeTreatmentCreator());
        map.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        map.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, isEligibleForOnLabelDrugCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, hasHadSomeApprovedTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES, hasHadSomeSystemicTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, hasHadLimitedSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, hasHadSpecificTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, hasHadTreatmentWithCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y, hasHadTreatmentCategoryOfTypeCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPE_Y, hasHadTreatmentCategoryIgnoringTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES, hasHadSomeTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES, hasHadLimitedTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_LEAST_Z_LINES,
                hasHadSomeTreatmentsOfCategoryWithTypeCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_MOST_Z_LINES,
                hasHadLimitedTreatmentsOfCategoryWithTypeCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES,
                hasHadLimitedTreatmentsOfCategoryWithTypesCreator());
        map.put(EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT, hadHadIntratumoralInjectionTreatmentCreator());
        map.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, participatesInAnotherTrialCreator());
        map.put(EligibilityRule.HAS_PARTICIPATED_IN_CURRENT_TRIAL, hasParticipatedInCurrentTrialCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator isEligibleForCurativeTreatmentCreator() {
        return function -> new IsEligibleForCurativeTreatment();
    }

    @NotNull
    private static FunctionCreator hasExhaustedSOCTreatmentsCreator() {
        return function -> new HasExhaustedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator isEligibleForOnLabelDrugCreator() {
        return function -> new IsEligibleForOnLabelDrug();
    }

    @NotNull
    private static FunctionCreator hasHadSomeApprovedTreatmentCreator() {
        return function -> new HasHadSomeApprovedTreatments();
    }

    @NotNull
    private static FunctionCreator hasHadSomeSystemicTreatmentCreator() {
        return function -> {
            int minSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadSomeSystemicTreatments(minSystemicTreatments));
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedSystemicTreatmentsCreator() {
        return function -> {
            int maxSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadLimitedSystemicTreatments(maxSystemicTreatments));
        };
    }

    @NotNull
    private static FunctionCreator hasHadSpecificTreatmentCreator() {
        return function -> {
            String treatment = FunctionInputResolver.createOneStringInput(function);
            return new PassOrFailEvaluationFunction(new HasHadSpecificTreatment(treatment));
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentWithCategoryCreator() {
        return function -> {
            TreatmentCategory category = FunctionInputResolver.createOneTreatmentInput(function).mappedCategory();
            return new PassOrFailEvaluationFunction(new HasHadSomeTreatmentsWithCategory(category, 1));
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryOfTypeCreator() {
        return function -> {
            OneTreatmentOneString input = FunctionInputResolver.createOneTreatmentOneStringInput(function);
            return new PassOrFailEvaluationFunction(new HasHadTreatmentWithCategoryOfType(input.treatment().mappedCategory(), input.string()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryIgnoringTypesCreator() {
        return function -> {
            OneTreatmentManyStrings input = FunctionInputResolver.createOneTreatmentManyStringsInput(function);
            return new PassOrFailEvaluationFunction(new HasHadTreatmentWithCategoryButNotOfTypes(input.treatment().mappedCategory(),
                    input.strings()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = FunctionInputResolver.createOneTreatmentOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadSomeTreatmentsWithCategory(input.treatment().mappedCategory(), input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = FunctionInputResolver.createOneTreatmentOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadLimitedTreatmentsWithCategory(input.treatment().mappedCategory(), input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryWithTypeCreator() {
        return function -> {
            OneTreatmentOneStringOneInteger input =
                    FunctionInputResolver.createOneTreatmentOneStringOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadSomeTreatmentsWithCategoryOfType(input.treatment().mappedCategory(),
                    input.string(),
                    input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedTreatmentsOfCategoryWithTypeCreator() {
        return function -> {
            OneTreatmentOneStringOneInteger input =
                    FunctionInputResolver.createOneTreatmentOneStringOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadLimitedTreatmentsWithCategoryOfTypes(input.treatment().mappedCategory(),
                    Lists.newArrayList(input.string()),
                    input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedTreatmentsOfCategoryWithTypesCreator() {
        return function -> {
            OneTreatmentManyStringsOneInteger input =
                    FunctionInputResolver.createOneTreatmentManyStringsOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadLimitedTreatmentsWithCategoryOfTypes(input.treatment().mappedCategory(),
                    input.strings(),
                    input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hadHadIntratumoralInjectionTreatmentCreator() {
        return function -> new HadHadIntratumoralInjectionTreatment();
    }

    @NotNull
    private static FunctionCreator participatesInAnotherTrialCreator() {
        return function -> new ParticipatesInAnotherTrial();
    }

    @NotNull
    private static FunctionCreator hasParticipatedInCurrentTrialCreator() {
        return function -> new HasParticipatedInCurrentTrial();
    }
}
