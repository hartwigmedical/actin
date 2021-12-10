package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionRuleMapping {

    private static final String GILBERT_DISEASE_DOID = "2739";
    private static final String AUTOIMMUNE_DOID = "417";
    private static final String CARDIAC_DISEASE_DOID = "114";
    private static final String VASCULAR_DISEASE_DOID = "114";
    private static final String CARDIOVASCULAR_DISEASE_DOID = "1287";
    private static final String LUNG_DISEASE_DOID = "850";
    private static final String TIA_DOID = "224";
    private static final String STROKE_DOID = "6713";
    private static final String HYPERTENSION_DOID = "10763";
    private static final String DIABETES_DOID = "9351";

    private OtherConditionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, hasSignificantConcomitantIllnessCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasPriorConditionDOIDCreator(doidModel, AUTOIMMUNE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasPriorConditionDOIDCreator(doidModel, CARDIAC_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE,
                hasPriorConditionDOIDCreator(doidModel, CARDIOVASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, hasPriorConditionDOIDCreator(doidModel, VASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasPriorConditionDOIDCreator(doidModel, LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasPriorConditionDOIDCreator(doidModel, STROKE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_TIA, hasPriorConditionDOIDCreator(doidModel, TIA_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_X, hasPriorConditionWithConfiguredDOIDCreator(doidModel));
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasPriorConditionDOIDCreator(doidModel, GILBERT_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, notImplementedCreator());
        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, hasCardiacArrhythmiaCreator());
        map.put(EligibilityRule.HAS_HYPERTENSION, hasPriorConditionDOIDCreator(doidModel, HYPERTENSION_DOID));
        map.put(EligibilityRule.HAS_DIABETES, hasPriorConditionDOIDCreator(doidModel, DIABETES_DOID));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, hasSufficientLVEFCreator(false));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, hasSufficientLVEFCreator(true));
        map.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, hasKnownMalabsorptionSyndromeCreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithConfiguredDOIDCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToFind = FunctionInputResolver.createOneStringInput(function);
            return new PriorDoidEvaluationFunction(doidModel, doidToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasSignificantConcomitantIllnessCreator() {
        return function -> new HasSignificantConcomitantIllness();
    }

    @NotNull
    private static FunctionCreator hasPriorConditionDOIDCreator(@NotNull DoidModel doidModel, @NotNull String doidToFind) {
        return function -> new PriorDoidEvaluationFunction(doidModel, doidToFind);
    }

    @NotNull
    private static FunctionCreator hasCardiacArrhythmiaCreator() {
        return function -> new HasCardiacArrhythmia();
    }

    @NotNull
    private static FunctionCreator hasKnownMalabsorptionSyndromeCreator() {
        return function -> new HasKnownMalabsorptionSyndrome();
    }

    @NotNull
    private static FunctionCreator hasSufficientLVEFCreator(boolean passIfUnknown) {
        return function -> {
            double minLVEF = FunctionInputResolver.createOneDoubleInput(function);
            return new HasSufficientLVEF(minLVEF, passIfUnknown);
        };
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
