package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryManyStrings;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneInteger;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneString;

import org.jetbrains.annotations.NotNull;

public final class TreatmentRuleMapping {

    private TreatmentRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT, isEligibleForCurativeTreatmentCreator());
        map.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        map.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, hasDeclinedSOCTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, hasHadSomeApprovedTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES, hasHadSomeSystemicTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, hasHadLimitedSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, hasHadSpecificTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, hasHadTreatmentCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y, hasHadTreatmentCategoryOfTypeCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPE_Y, hasHadTreatmentCategoryIgnoringTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES, hasHadSomeTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_LEAST_Z_LINES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_MOST_Z_LINES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_FLUOROPYRIMIDINE_TREATMENT, hasHadFluoropyrimidineTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_TAXANE_TREATMENT, function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_TAXANE_TREATMENT_AND_AT_MOST_X_LINES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_TYROSINE_KINASE_TREATMENT,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_INTRATUMURAL_INJECTION_TREATMENT,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, isEligibleForOnLabelDrugCreator());

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
    private static FunctionCreator hasDeclinedSOCTreatmentsCreator() {
        return function -> new HasDeclinedSOCTreatments();
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
    private static FunctionCreator hasHadTreatmentCategoryCreator() {
        return function -> {
            TreatmentCategory category = FunctionInputResolver.createOneTreatmentCategoryInput(function);
            return new PassOrFailEvaluationFunction(new HasHadTreatmentCategoryOfType(category, null));
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryOfTypeCreator() {
        return function -> {
            OneTreatmentCategoryOneString input = FunctionInputResolver.createOneTreatmentCategoryOneStringInput(function);
            return new PassOrFailEvaluationFunction(new HasHadTreatmentCategoryOfType(input.treatmentCategory(), input.string()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryIgnoringTypesCreator() {
        return function -> {
            OneTreatmentCategoryManyStrings input = FunctionInputResolver.createOneTreatmentCategoryManyStringsInput(function);
            return new PassOrFailEvaluationFunction(new HasHadTreatmentCategoryButNotOfTypes(input.treatmentCategory(), input.strings()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentCategoryOneInteger input = FunctionInputResolver.createOneTreatmentCategoryOneIntegerInput(function);
            return new PassOrFailEvaluationFunction(new HasHadSomeTreatmentsWithCategory(input.treatmentCategory(), input.integer()));
        };
    }

    @NotNull
    private static FunctionCreator hasHadFluoropyrimidineTreatmentCreator() {
        return function -> new PassOrFailEvaluationFunction(new HasHadFluoropyrimidineTreatment());
    }

    @NotNull
    private static FunctionCreator isEligibleForOnLabelDrugCreator() {
        return function -> new IsEligibleForOnLabelDrug();
    }
}
