package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionRuleMapping {

    private static final String GILBERT_DISEASE_DOID = "2739";
    private static final String AUTOIMMUNE_DISEASE_DOID = "417";
    private static final String CARDIAC_DISEASE_DOID = "114";
    private static final String VASCULAR_DISEASE_DOID = "114";
    private static final String CARDIOVASCULAR_DISEASE_DOID = "1287";
    private static final String LUNG_DISEASE_DOID = "850";
    private static final String LIVER_DISEASE_DOID = "409";
    private static final String TIA_DOID = "224";
    private static final String STROKE_DOID = "6713";
    private static final String HYPERTENSION_DOID = "10763";
    private static final String DIABETES_DOID = "9351";

    private OtherConditionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasSpecificPriorConditionCreator(doidModel, AUTOIMMUNE_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasSpecificPriorConditionCreator(doidModel, CARDIAC_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE,
                hasSpecificPriorConditionCreator(doidModel, CARDIOVASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, hasSpecificPriorConditionCreator(doidModel, VASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasSpecificPriorConditionCreator(doidModel, LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE, hasSpecificPriorConditionCreator(doidModel, LIVER_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasSpecificPriorConditionCreator(doidModel, STROKE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_TIA, hasSpecificPriorConditionCreator(doidModel, TIA_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_X, hasPriorConditionWithConfiguredDOIDCreator(doidModel));
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasSpecificPriorConditionCreator(doidModel, GILBERT_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, hasHadOrganTransplantCreator());
        map.put(EligibilityRule.HAS_HYPERTENSION, hasSpecificPriorConditionCreator(doidModel, HYPERTENSION_DOID));
        map.put(EligibilityRule.HAS_DIABETES, hasSpecificPriorConditionCreator(doidModel, DIABETES_DOID));
        map.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, hasKnownMalabsorptionSyndromeCreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, isInDialysisCreator());
        map.put(EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION, hasSevereConcomitantIllnessCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSevereConcomitantIllnessCreator() {
        return function -> new HasSevereConcomitantIllness();
    }

    @NotNull
    private static FunctionCreator hasSpecificPriorConditionCreator(@NotNull DoidModel doidModel, @NotNull String doidToFind) {
        return function -> new HasHadSpecificPriorCondition(doidModel, doidToFind);
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithConfiguredDOIDCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasHadSpecificPriorCondition(doidModel, doidToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasHadOrganTransplantCreator() {
        return function -> new HasHadOrganTransplant();
    }

    @NotNull
    private static FunctionCreator hasKnownMalabsorptionSyndromeCreator() {
        return function -> new HasKnownMalabsorptionSyndrome();
    }

    @NotNull
    private static FunctionCreator isInDialysisCreator() {
        return function -> new IsInDialysis();
    }
}
