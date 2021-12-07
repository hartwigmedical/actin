package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionRuleMapping {

    private OtherConditionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        DoidEvaluator doidEvaluator = new DoidEvaluator(doidModel);

        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, hasSignificantConcomitantIllnessCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasHistoryOfAutoimmuneDiseaseCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasHistoryOfCardiacDiseaseCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, hasHistoryOfCardiovascularDiseaseCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, notImplementedCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasHistoryOfLungDiseaseCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasHistoryOfStrokeCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HISTORY_OF_TIA, hasHistoryOfTiaCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasGilbertDiseaseCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, notImplementedCreator());
        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, hasCardiacArrhythmiaCreator());
        map.put(EligibilityRule.HAS_HYPERTENSION, hasHypertensionCreator(doidEvaluator));
        map.put(EligibilityRule.HAS_DIABETES, notImplementedCreator());
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, hasSufficientLVEFCreator(false));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, hasSufficientLVEFCreator(true));
        map.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, hasKnownMalabsorptionSyndromeCreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSignificantConcomitantIllnessCreator() {
        return function -> new HasSignificantConcomitantIllness();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfAutoimmuneDiseaseCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfAutoimmuneDisease(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasHistoryOfCardiacDiseaseCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfCardiacDisease(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasHistoryOfCardiovascularDiseaseCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfCardiovascularDisease(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasHistoryOfLungDiseaseCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfLungDisease(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasHistoryOfStrokeCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfStroke(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasHistoryOfTiaCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHistoryOfTia(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasGilbertDiseaseCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasGilbertDisease(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasCardiacArrhythmiaCreator() {
        return function -> new HasCardiacArrhythmia();
    }

    @NotNull
    private static FunctionCreator hasHypertensionCreator(@NotNull DoidEvaluator doidEvaluator) {
        return function -> new HasHypertension(doidEvaluator);
    }

    @NotNull
    private static FunctionCreator hasKnownMalabsorptionSyndromeCreator() {
        return function -> new HasKnownMalabsorptionSyndrome();
    }

    @NotNull
    private static FunctionCreator hasSufficientLVEFCreator(boolean passIfUnknown) {
        return function -> {
            double minLVEF = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientLVEF(minLVEF, passIfUnknown);
        };
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
