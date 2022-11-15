package com.hartwig.actin.algo.evaluation.othercondition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class OtherConditionRuleMapper extends RuleMapper {

    private static final String HYPOTENSION_NAME = "hypotension";

    public OtherConditionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X, hasPriorConditionWithConfiguredDOIDTermCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME, hasPriorConditionWithConfiguredNameCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.AUTOIMMUNE_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_ANGINA, hasHistoryOfAnginaCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_BRAIN_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.BRAIN_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.HEART_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.CARDIOVASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X,
                hasHistoryOfCongestiveHeartFailureWithNYHACreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.CENTRAL_NERVOUS_SYSTEM_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.EYE_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.GASTROINTESTINAL_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.IMMUNE_SYSTEM_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE,
                hasPriorConditionWithDoidCreator(DoidConstants.INTERSTITIAL_LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.LIVER_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT, hasPriorConditionWithDoidCreator(DoidConstants.MYOCARDIAL_INFARCT_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS,
                hasRecentPriorConditionWithDoidCreator(DoidConstants.MYOCARDIAL_INFARCT_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS, hasHistoryOfPneumonitisCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasPriorConditionWithDoidCreator(DoidConstants.STROKE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT, hasHistoryOfThromboembolicEventCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT, hasHistoryOfThromboembolicEventCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT, hasHistoryOfThromboembolicEventCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.VASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION, hasSevereConcomitantIllnessCreator());
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, hasHadOrganTransplantCreator());
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS, hasHadOrganTransplantWithinYearsCreator());
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasPriorConditionWithDoidCreator(DoidConstants.GILBERT_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HYPERTENSION, hasPriorConditionWithDoidCreator(DoidConstants.HYPERTENSION_DOID));
        map.put(EligibilityRule.HAS_HYPOTENSION, hasPriorConditionWithNameCreator(HYPOTENSION_NAME));
        map.put(EligibilityRule.HAS_DIABETES, hasPriorConditionWithDoidCreator(DoidConstants.DIABETES_DOID));
        map.put(EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES, hasPotentialAbsorptionDifficultiesCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES, hasOralMedicationDifficultiesCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT, hasContraindicationToCTCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI, hasContraindicationToMRICreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI, hasContraindicationToMRICreator());
        map.put(EligibilityRule.HAS_POST_OPERATIVE_BASELINE_CONTRAST_ENHANCED_MRI_SCAN,
                hasPostOperativeBaselineContrastEnhancedMRIScanCreator());
        map.put(EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE, hasMRIScanDocumentingStableDiseaseCreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, isInDialysisCreator());
        map.put(EligibilityRule.HAS_ADEQUATE_VEIN_ACCESS_FOR_LEUKAPHERESIS, hasAdequateVeinAccessCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasPriorConditionWithConfiguredDOIDTermCreator() {
        return function -> {
            String doidTermToFind = functionInputResolver().createOneDoidTermInput(function);
            return new HasHadPriorConditionWithDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToFind));
        };
    }

    @NotNull
    private FunctionCreator hasPriorConditionWithConfiguredNameCreator() {
        return function -> {
            String nameToFind = functionInputResolver().createOneStringInput(function);
            return new HasHadPriorConditionWithName(nameToFind);
        };
    }

    //TODO: Update according to README
    @NotNull
    private FunctionCreator hasPriorConditionWithDoidCreator(@NotNull String doidToFind) {
        return function -> new HasHadPriorConditionWithDoid(doidModel(), doidToFind);
    }

    @NotNull
    private FunctionCreator hasRecentPriorConditionWithDoidCreator(@NotNull String doidToFind) {
        return function -> {
            int maxMonthsAgo = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo);
            return new HasHadPriorConditionWithDoidRecently(doidModel(), doidToFind, minDate);
        };
    }

    @NotNull
    private FunctionCreator hasHistoryOfAnginaCreator() {
        return function -> {
            List<EvaluationFunction> functions = Lists.newArrayList();
            functions.add(new HasHadPriorConditionWithDoid(doidModel(), DoidConstants.INTERMEDIATE_CORONARY_SYNDROME_DOID));
            functions.add(new HasHadPriorConditionWithDoid(doidModel(), DoidConstants.PRINZMETAL_ANGINA_DOID));
            return new Or(functions);
        };
    }

    @NotNull
    private FunctionCreator hasHistoryOfCongestiveHeartFailureWithNYHACreator() {
        return function -> new HasHistoryOfCongestiveHeartFailureWithNYHA();
    }

    @NotNull
    private FunctionCreator hasPriorConditionWithNameCreator(@NotNull String nameToFind) {
        return function -> new HasHadPriorConditionWithName(nameToFind);
    }

    @NotNull
    private FunctionCreator hasHistoryOfPneumonitisCreator() {
        return function -> new HasHistoryOfPneumonitis(doidModel());
    }

    @NotNull
    private FunctionCreator hasHistoryOfThromboembolicEventCreator() {
        return function -> new HasHistoryOfThromboembolicEvent();
    }

    @NotNull
    private FunctionCreator hasSevereConcomitantIllnessCreator() {
        return function -> new HasSevereConcomitantIllness();
    }

    @NotNull
    private FunctionCreator hasHadOrganTransplantCreator() {
        return function -> new HasHadOrganTransplant(null);
    }

    @NotNull
    private FunctionCreator hasHadOrganTransplantWithinYearsCreator() {
        return function -> {
            int maxYearsAgo = functionInputResolver().createOneIntegerInput(function);
            int minYear = referenceDateProvider().year() - maxYearsAgo;
            return new HasHadOrganTransplant(minYear);
        };
    }

    @NotNull
    private FunctionCreator hasPotentialAbsorptionDifficultiesCreator() {
        return function -> new HasPotentialAbsorptionDifficulties(doidModel());
    }

    @NotNull
    private FunctionCreator hasOralMedicationDifficultiesCreator() {
        return function -> new HasOralMedicationDifficulties();
    }

    @NotNull
    private FunctionCreator hasContraindicationToCTCreator() {
        return function -> new HasContraindicationToCT(doidModel());
    }

    @NotNull
    private FunctionCreator hasContraindicationToMRICreator() {
        return function -> new HasContraindicationToMRI(doidModel());
    }

    @NotNull
    private FunctionCreator hasPostOperativeBaselineContrastEnhancedMRIScanCreator() {
        return function -> new HasPostOperativeBaselineContrastEnhancedMRIScan();
    }

    @NotNull
    private FunctionCreator hasMRIScanDocumentingStableDiseaseCreator() {
        return function -> new HasMRIScanDocumentingStableDisease();
    }

    @NotNull
    private FunctionCreator isInDialysisCreator() {
        return function -> new IsInDialysis();
    }

    @NotNull
    private FunctionCreator hasAdequateVeinAccessCreator() {
        return function -> new HasAdequateVeinAccess();
    }
}
