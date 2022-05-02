package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionRuleMapping {

    private static final String GILBERT_DISEASE_DOID = "2739";
    private static final String AUTOIMMUNE_DISEASE_DOID = "417";
    private static final String CARDIAC_DISEASE_DOID = "114";
    private static final String VASCULAR_DISEASE_DOID = "114";
    private static final String CARDIOVASCULAR_DISEASE_DOID = "1287";
    private static final String CENTRAL_NERVOUS_SYSTEM_DOID = "331";
    private static final String LUNG_DISEASE_DOID = "850";
    private static final String LIVER_DISEASE_DOID = "409";
    private static final String TIA_DOID = "224";
    private static final String MYOCARDIAL_INFARCT_DOID = "5844";
    private static final String STROKE_DOID = "6713";
    private static final String HYPERTENSION_DOID = "10763";
    private static final String DIABETES_DOID = "9351";
    private static final String GASTROINTESTINAL_DISEASE_DOID = "77";
    private static final String IMMUNE_SYSTEM_DISEASE_DOID = "2914";

    private static final String HYPOTENSION_NAME = "hypotension";

    private OtherConditionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_X, hasPriorConditionWithConfiguredDOIDCreator(doidModel));
        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME, hasPriorConditionWithConfiguredNameCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasPriorConditionWithDoidCreator(doidModel, AUTOIMMUNE_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasPriorConditionWithDoidCreator(doidModel, CARDIAC_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE,
                hasPriorConditionWithDoidCreator(doidModel, CARDIOVASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE,
                hasPriorConditionWithDoidCreator(doidModel, CENTRAL_NERVOUS_SYSTEM_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE,
                hasPriorConditionWithDoidCreator(doidModel, GASTROINTESTINAL_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE,
                hasPriorConditionWithDoidCreator(doidModel, IMMUNE_SYSTEM_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE, hasPriorConditionWithDoidCreator(doidModel, LIVER_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasPriorConditionWithDoidCreator(doidModel, LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT, hasPriorConditionWithDoidCreator(doidModel, MYOCARDIAL_INFARCT_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasPriorConditionWithDoidCreator(doidModel, STROKE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_TIA, hasPriorConditionWithDoidCreator(doidModel, TIA_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, hasPriorConditionWithDoidCreator(doidModel, VASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION, hasSevereConcomitantIllnessCreator());
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, hasHadOrganTransplantCreator());
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasPriorConditionWithDoidCreator(doidModel, GILBERT_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HYPERTENSION, hasPriorConditionWithDoidCreator(doidModel, HYPERTENSION_DOID));
        map.put(EligibilityRule.HAS_HYPOTENSION, hasPriorConditionWithNameCreator(HYPOTENSION_NAME));
        map.put(EligibilityRule.HAS_DIABETES, hasPriorConditionWithDoidCreator(doidModel, DIABETES_DOID));
        map.put(EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES, hasPotentialAbsorptionDifficultiesCreator(doidModel));
        map.put(EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES, hasOralMedicationDifficultiesCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT, hasContraindicationToCTCreator(doidModel));
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI, hasContraindicationToMRICreator(doidModel));
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI, hasContraindicationToPETMRICreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, isInDialysisCreator());
        map.put(EligibilityRule.HAS_ADEQUATE_VEIN_ACCESS_FOR_LEUKAPHERESIS, hasAdequateVeinAccessCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithConfiguredDOIDCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasHadPriorConditionWithDoid(doidModel, doidToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithConfiguredNameCreator() {
        return function -> {
            String nameToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasHadPriorConditionWithName(nameToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithDoidCreator(@NotNull DoidModel doidModel, @NotNull String doidToFind) {
        return function -> new HasHadPriorConditionWithDoid(doidModel, doidToFind);
    }

    @NotNull
    private static FunctionCreator hasPriorConditionWithNameCreator(@NotNull String nameToFind) {
        return function -> new HasHadPriorConditionWithName(nameToFind);
    }

    @NotNull
    private static FunctionCreator hasSevereConcomitantIllnessCreator() {
        return function -> new HasSevereConcomitantIllness();
    }

    @NotNull
    private static FunctionCreator hasHadOrganTransplantCreator() {
        return function -> new HasHadOrganTransplant();
    }

    @NotNull
    private static FunctionCreator hasPotentialAbsorptionDifficultiesCreator(@NotNull DoidModel doidModel) {
        return function -> new HasPotentialAbsorptionDifficulties(doidModel);
    }

    @NotNull
    private static FunctionCreator hasOralMedicationDifficultiesCreator() {
        return function -> new HasOralMedicationDifficulties();
    }

    @NotNull
    private static FunctionCreator hasContraindicationToCTCreator(@NotNull DoidModel doidModel) {
        return function -> new HasContraindicationToCT(doidModel);
    }

    @NotNull
    private static FunctionCreator hasContraindicationToMRICreator(@NotNull DoidModel doidModel) {
        return function -> new HasContraindicationToMRI(doidModel);
    }

    @NotNull
    private static FunctionCreator hasContraindicationToPETMRICreator() {
        return function -> new HasContraindicationToPETMRI();
    }

    @NotNull
    private static FunctionCreator isInDialysisCreator() {
        return function -> new IsInDialysis();
    }

    @NotNull
    private static FunctionCreator hasAdequateVeinAccessCreator() {
        return function -> new HasAdequateVeinAccess();
    }
}
