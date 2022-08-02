package com.hartwig.actin.algo.evaluation.othercondition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class OtherConditionRuleMapper extends RuleMapper {

    private static final String AUTOIMMUNE_DISEASE_DOID = "417";
    private static final String INTERMEDIATE_CORONARY_SYNDROME_DOID = "8805";
    private static final String PRINZMETAL_ANGINA_DOID = "0111151";
    private static final String BRAIN_DISEASE_DOID = "936";
    private static final String CARDIAC_DISEASE_DOID = "114";
    private static final String CARDIOVASCULAR_DISEASE_DOID = "1287";
    private static final String CENTRAL_NERVOUS_SYSTEM_DOID = "331";
    private static final String DIABETES_DOID = "9351";
    private static final String GASTROINTESTINAL_DISEASE_DOID = "77";
    private static final String GILBERT_DISEASE_DOID = "2739";
    private static final String HYPERTENSION_DOID = "10763";
    private static final String IMMUNE_SYSTEM_DISEASE_DOID = "2914";
    private static final String INTERSTITIAL_LUNG_DISEASE_DOID = "3082";
    private static final String LUNG_DISEASE_DOID = "850";
    private static final String LIVER_DISEASE_DOID = "409";
    private static final String MYOCARDIAL_INFARCT_DOID = "5844";
    private static final String STROKE_DOID = "6713";
    private static final String VASCULAR_DISEASE_DOID = "114";

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
        map.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasPriorConditionWithDoidCreator(AUTOIMMUNE_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_ANGINA, hasHistoryOfAnginaCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_BRAIN_DISEASE, hasPriorConditionWithDoidCreator(BRAIN_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasPriorConditionWithDoidCreator(CARDIAC_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, hasPriorConditionWithDoidCreator(CARDIOVASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X,
                hasHistoryOfCongestiveHeartFailureWithNYHACreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE,
                hasPriorConditionWithDoidCreator(CENTRAL_NERVOUS_SYSTEM_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE, hasPriorConditionWithDoidCreator(GASTROINTESTINAL_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE, hasPriorConditionWithDoidCreator(IMMUNE_SYSTEM_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE, hasPriorConditionWithDoidCreator(INTERSTITIAL_LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE, hasPriorConditionWithDoidCreator(LIVER_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, hasPriorConditionWithDoidCreator(LUNG_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT, hasPriorConditionWithDoidCreator(MYOCARDIAL_INFARCT_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS,
                hasRecentPriorConditionWithDoidCreator(MYOCARDIAL_INFARCT_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS, hasHistoryOfPneumonitisCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_STROKE, hasPriorConditionWithDoidCreator(STROKE_DOID));
        map.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, hasPriorConditionWithDoidCreator(VASCULAR_DISEASE_DOID));
        map.put(EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION, hasSevereConcomitantIllnessCreator());
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, hasHadOrganTransplantCreator());
        map.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS, hasHadOrganTransplantWithinYearsCreator());
        map.put(EligibilityRule.HAS_GILBERT_DISEASE, hasPriorConditionWithDoidCreator(GILBERT_DISEASE_DOID));
        map.put(EligibilityRule.HAS_HYPERTENSION, hasPriorConditionWithDoidCreator(HYPERTENSION_DOID));
        map.put(EligibilityRule.HAS_HYPOTENSION, hasPriorConditionWithNameCreator(HYPOTENSION_NAME));
        map.put(EligibilityRule.HAS_DIABETES, hasPriorConditionWithDoidCreator(DIABETES_DOID));
        map.put(EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES, hasPotentialAbsorptionDifficultiesCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES, hasOralMedicationDifficultiesCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT, hasContraindicationToCTCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI, hasContraindicationToMRICreator());
        map.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI, hasContraindicationToMRICreator());
        map.put(EligibilityRule.IS_IN_DIALYSIS, isInDialysisCreator());
        map.put(EligibilityRule.HAS_HAD_RECENT_TRAUMA, hasHadRecentTraumaCreator());
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
            functions.add(new HasHadPriorConditionWithDoid(doidModel(), INTERMEDIATE_CORONARY_SYNDROME_DOID));
            functions.add(new HasHadPriorConditionWithDoid(doidModel(), PRINZMETAL_ANGINA_DOID));
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
    private FunctionCreator isInDialysisCreator() {
        return function -> new IsInDialysis();
    }

    @NotNull
    private FunctionCreator hasHadRecentTraumaCreator() {
        return function -> new HasHadRecentTrauma();
    }

    @NotNull
    private FunctionCreator hasAdequateVeinAccessCreator() {
        return function -> new HasAdequateVeinAccess();
    }
}
