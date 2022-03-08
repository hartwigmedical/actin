package com.hartwig.actin.algo.evaluation.medication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluationFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class MedicationRuleMapping {

    // Medication categories
    private static final String ANTICOAGULANTS = "Anticoagulants";
    private static final String CORTICOSTEROIDS = "Corticosteroids";
    private static final String VITAMIN_K_ANTAGONISTS = "Vitamin K Antagonists";
    private static final String TRIAZOLES = "Triazoles";
    private static final String CUTANEOUS_IMIDAZOLES = "Imidazoles, cutaneous";
    private static final String OTHER_IMIDAZOLES = "Imidazoles, other";
    private static final String BISPHOSPHONATES = "Bisphosphonates";
    private static final String CALCIUM_REGULATORY_MEDICATION = "Calcium regulatory medication";
    private static final String GONADORELIN_ANTAGONISTS = "Gonadorelin antagonists";
    private static final String GONADORELIN_AGONISTS = "Gonadorelin agonists";
    private static final String SELECTIVE_IMMUNOSUPPRESSANTS = "Immunosuppressants, selective";
    private static final String OTHER_IMMUNOSUPPRESSANTS = "Immunosuppressants, other";

    // Medication names
    private static final String PROBENECID = "Probenecid";
    private static final String RIFAMPICIN = "Rifampicin";
    private static final String NOVOBIOCIN = "Novobiocin";
    private static final String CABOTEGRAVIR = "Cabotegravir";

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION, getsActiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION, getsActiveMedicationWithConfiguredNameCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, getsActiveMedicationWithApproximateCategoryCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS));
        map.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES));
        map.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION));
        map.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, getsActiveMedicationWithExactCategoryCreator(CORTICOSTEROIDS));
        map.put(EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(VITAMIN_K_ANTAGONISTS));
        map.put(EligibilityRule.CURRENTLY_GETS_DISEASE_MODIFYING_AGENTS, getsDiseaseModifyingAgentsCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS));
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION,
                getsActiveMedicationWithExactCategoryCreator(SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS));
        map.put(EligibilityRule.CURRENTLY_GETS_OAT3_INHIBITORS_MEDICATION,
                getsActiveMedicationWithNamesCreator(PROBENECID, RIFAMPICIN, NOVOBIOCIN, CABOTEGRAVIR));
        map.put(EligibilityRule.CURRENTLY_GETS_PAIN_MEDICATION,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, getsProhibitedMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_COLONY_STIMULATING_FACTORS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, getsCYPXInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_OATP_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, getsPGPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, getsActiveAndStableMedicationOfCategoryCreator(ANTICOAGULANTS));
        map.put(EligibilityRule.HAS_STABLE_PAIN_MEDICATION_DOSING,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));

        return map;
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationCreator() {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsAnyMedication());
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithConfiguredNameCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationWithName(Sets.newHashSet(termToFind)));
        };
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithNamesCreator(@NotNull String... termsToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationWithName(Sets.newHashSet(termsToFind)));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithApproximateCategoryCreator() {
        return function -> {
            String categoryTermToFind = FunctionInputResolver.createOneStringInput(function);
            return new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationOfApproximateCategory(categoryTermToFind));
        };
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithExactCategoryCreator(@NotNull String... categoriesToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationOfExactCategory(Sets.newHashSet(categoriesToFind)));
    }

    @NotNull
    private static FunctionCreator getsDiseaseModifyingAgentsCreator() {
        return function -> new CurrentlyGetsDiseaseModifyingAgents();
    }

    @NotNull
    private static FunctionCreator getsProhibitedMedicationCreator() {
        return function -> new CurrentlyGetsProhibitedMedicationCreator();
    }

    @NotNull
    private static FunctionCreator getsPGPInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsPGPInhibitingMedication();
    }

    @NotNull
    private static FunctionCreator getsCYPXInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsCYPXInhibitingMedication();
    }

    @NotNull
    private static FunctionCreator getsActiveAndStableMedicationOfCategoryCreator(@NotNull String categoryToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsStableMedicationOfCategory(categoryToFind));
    }
}
