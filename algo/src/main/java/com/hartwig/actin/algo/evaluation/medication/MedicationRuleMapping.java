package com.hartwig.actin.algo.evaluation.medication;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.composite.Or;
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
    private static final String COLONY_STIMULATING_FACTORS = "Colony stimulating factors";

    private static final String PAIN_MEDICATION_CATEGORY_1 = "NSAIDs";
    private static final String PAIN_MEDICATION_CATEGORY_2 = "Opioids";

    // Medication names
    private static final String PROBENECID = "Probenecid";
    private static final String RIFAMPICIN = "Rifampicin";
    private static final String NOVOBIOCIN = "Novobiocin";
    private static final String CABOTEGRAVIR = "Cabotegravir";

    private static final String PAIN_MEDICATION_NAME_1 = "Paracetamol";
    private static final String PAIN_MEDICATION_NAME_2 = "Amitriptyline";
    private static final String PAIN_MEDICATION_NAME_3 = "Pregabalin";

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION, getsActiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION, getsActiveMedicationWithConfiguredNameCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, getsActiveMedicationWithApproximateCategoryCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsAnticoagulantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION, getsAzoleMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION, getsBoneResorptiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, getsCorticosteroidMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION, getsCoumarinDerivativeMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_DISEASE_MODIFYING_AGENTS, getsDiseaseModifyingAgentsCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION, getsGonadorelinMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, getsImmunosuppressantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_OAT3_INHIBITORS_MEDICATION, getsOAT3InhibitionMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_PAIN_MEDICATION, getsPainMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, getsProhibitedMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION, getsQTProlongatingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_COLONY_STIMULATING_FACTORS, getColonyStimulatingFactorsCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, getsCYPXInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, getsPGPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_OATP_X, getsOATPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP, getsBCRPInhibitingMedicationCreator());
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, getsStableDosingAnticoagulantMedicationCreator());
        map.put(EligibilityRule.HAS_STABLE_PAIN_MEDICATION_DOSING, getsStableDosingPainMedicationCreator());

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
            return new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationOfName(Sets.newHashSet(termToFind)));
        };
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithNamesCreator(@NotNull String... termsToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsMedicationOfName(Sets.newHashSet(termsToFind)));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithApproximateCategoryCreator() {
        return function -> {
            String categoryTermToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsMedicationOfApproximateCategory(categoryTermToFind);
        };
    }

    @NotNull
    private static FunctionCreator getsAnticoagulantMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsAzoleMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES);
    }

    @NotNull
    private static FunctionCreator getsBoneResorptiveMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION);
    }

    @NotNull
    private static FunctionCreator getsCorticosteroidMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(CORTICOSTEROIDS);
    }

    @NotNull
    private static FunctionCreator getsCoumarinDerivativeMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsGonadorelinMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS);
    }

    @NotNull
    private static FunctionCreator getsDiseaseModifyingAgentsCreator() {
        return function -> new CurrentlyGetsDiseaseModifyingAgents();
    }

    @NotNull
    private static FunctionCreator getsImmunosuppressantMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS);
    }

    @NotNull
    private static FunctionCreator getsOAT3InhibitionMedicationCreator() {
        return getsActiveMedicationWithNamesCreator(PROBENECID, RIFAMPICIN, NOVOBIOCIN, CABOTEGRAVIR);
    }

    @NotNull
    private static FunctionCreator getsPainMedicationCreator() {
        return function -> {
            EvaluationFunction categoryFunction =
                    getsActiveMedicationWithExactCategoryCreator(PAIN_MEDICATION_CATEGORY_1, PAIN_MEDICATION_CATEGORY_2).create(function);
            EvaluationFunction nameFunction =
                    getsActiveMedicationWithNamesCreator(PAIN_MEDICATION_NAME_1, PAIN_MEDICATION_NAME_2, PAIN_MEDICATION_NAME_3).create(
                            function);
            return new Or(Lists.newArrayList(categoryFunction, nameFunction));
        };
    }

    @NotNull
    private static FunctionCreator getsProhibitedMedicationCreator() {
        return function -> new CurrentlyGetsProhibitedMedicationCreator();
    }

    @NotNull
    private static FunctionCreator getsQTProlongatingMedicationCreator() {
        return function -> new CurrentlyGetsQTProlongatingMedication();
    }

    @NotNull
    private static FunctionCreator getColonyStimulatingFactorsCreator() {
        return getsActiveMedicationWithExactCategoryCreator(COLONY_STIMULATING_FACTORS);
    }

    @NotNull
    private static FunctionCreator getsCYPXInhibitingMedicationCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsCYPXInhibitingMedication(termToFind);
        };
    }

    @NotNull
    private static FunctionCreator getsPGPInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsPGPInhibitingMedication();
    }

    @NotNull
    private static FunctionCreator getsOATPInhibitingMedicationCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsOATPInhibitingMedication(termToFind);
        };
    }

    @NotNull
    private static FunctionCreator getsBCRPInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsBCRPInhibitingMedication();
    }

    @NotNull
    private static FunctionCreator getsStableDosingAnticoagulantMedicationCreator() {
        return getsStableMedicationOfCategoryCreator(ANTICOAGULANTS);
    }

    @NotNull
    private static FunctionCreator getsStableDosingPainMedicationCreator() {
        return function -> {
            EvaluationFunction categoryFunction =
                    getsStableMedicationOfCategoryCreator(PAIN_MEDICATION_CATEGORY_1, PAIN_MEDICATION_CATEGORY_2).create(function);
            EvaluationFunction nameFunction =
                    getsStableMedicationOfNameCreator(PAIN_MEDICATION_NAME_1, PAIN_MEDICATION_NAME_2, PAIN_MEDICATION_NAME_3).create(
                            function);
            return new Or(Lists.newArrayList(categoryFunction, nameFunction));
        };
    }

    @NotNull
    private static FunctionCreator getsStableMedicationOfCategoryCreator(@NotNull String... categoriesToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsStableMedicationOfCategory(Sets.newHashSet(categoriesToFind)));
    }

    @NotNull
    private static FunctionCreator getsStableMedicationOfNameCreator(@NotNull String... termsToFind) {
        return function -> new PassOrFailEvaluationFunction(new CurrentlyGetsStableMedicationOfName(Sets.newHashSet(termsToFind)));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithExactCategoryCreator(@NotNull String... categoriesToFind) {
        return function ->new CurrentlyGetsMedicationOfExactCategory(Sets.newHashSet(categoriesToFind));
    }
}
