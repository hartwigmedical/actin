package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class MedicationRuleMapping {

    // Medication categories
    private static final String ANTICOAGULANTS = "Anticoagulants";
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

    private static final String PAIN_MEDICATION_CATEGORY_1 = "NSAIDs";
    private static final String PAIN_MEDICATION_CATEGORY_2 = "Opioids";

    // Medication names
    private static final String PAIN_MEDICATION_NAME_1 = "Paracetamol";
    private static final String PAIN_MEDICATION_NAME_2 = "Amitriptyline";
    private static final String PAIN_MEDICATION_NAME_3 = "Pregabalin";
    private static final String PAIN_MEDICATION_NAME_4 = "Gabapentin";

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        LocalDate evaluationDate = referenceDateProvider.date();
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION, getsActiveMedicationWithConfiguredNameCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, getsActiveMedicationWithApproximateCategoryCreator(evaluationDate));
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS,
                hasReceivedMedicationWithApproximateCategoryWithinWeeksCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsAnticoagulantMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION, getsAzoleMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION, getsBoneResorptiveMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION, getsCoumarinDerivativeMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION, getsGonadorelinMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, getsImmunosuppressantMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_PAIN_MEDICATION, getsPainMedicationCreator(evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, getsProhibitedMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION, getsQTProlongatingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, getsCYPXInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, getsPGPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_OATP_X, getsOATPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP, getsBCRPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_DRUG_METABOLIZING_ENZYMES,
                getsDrugMetabolizingEnzymeInhibitingMedicationCreator(evaluationDate));
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, getsStableDosingAnticoagulantMedicationCreator(evaluationDate));

        return map;
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithConfiguredNameCreator(@NotNull LocalDate evaluationDate) {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsMedicationOfName(evaluationDate, Sets.newHashSet(termToFind));
        };
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithNamesCreator(@NotNull LocalDate evaluationDate, @NotNull String... termsToFind) {
        return function -> new CurrentlyGetsMedicationOfName(evaluationDate, Sets.newHashSet(termsToFind));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithApproximateCategoryCreator(@NotNull LocalDate evaluationDate) {
        return function -> {
            String categoryTermToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsMedicationOfApproximateCategory(evaluationDate, categoryTermToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasReceivedMedicationWithApproximateCategoryWithinWeeksCreator() {
        return function -> new HasReceivedMedicationWithApproximateCategoryWithinWeeks();
    }

    @NotNull
    private static FunctionCreator getsAnticoagulantMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsAzoleMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES);
    }

    @NotNull
    private static FunctionCreator getsBoneResorptiveMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION);
    }

    @NotNull
    private static FunctionCreator getsCoumarinDerivativeMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsGonadorelinMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS);
    }

    @NotNull
    private static FunctionCreator getsImmunosuppressantMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsActiveMedicationWithExactCategoryCreator(evaluationDate, SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS);
    }

    @NotNull
    private static FunctionCreator getsPainMedicationCreator(@NotNull LocalDate evaluationDate) {
        return function -> {
            EvaluationFunction categoryFunction = getsActiveMedicationWithExactCategoryCreator(evaluationDate,
                    PAIN_MEDICATION_CATEGORY_1,
                    PAIN_MEDICATION_CATEGORY_2).create(function);
            EvaluationFunction nameFunction = getsActiveMedicationWithNamesCreator(evaluationDate,
                    PAIN_MEDICATION_NAME_1,
                    PAIN_MEDICATION_NAME_2,
                    PAIN_MEDICATION_NAME_3,
                    PAIN_MEDICATION_NAME_4).create(function);
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
    private static FunctionCreator getsDrugMetabolizingEnzymeInhibitingMedicationCreator(@NotNull LocalDate evaluationDate) {
        return function -> new CurrentlyGetsMetabolizingEnzymeInhibitingMedication(evaluationDate);
    }

    @NotNull
    private static FunctionCreator getsStableDosingAnticoagulantMedicationCreator(@NotNull LocalDate evaluationDate) {
        return getsStableMedicationOfCategoryCreator(evaluationDate, ANTICOAGULANTS);
    }

    @NotNull
    private static FunctionCreator getsStableMedicationOfCategoryCreator(@NotNull LocalDate evaluationDate,
            @NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsStableMedicationOfCategory(evaluationDate, Sets.newHashSet(categoriesToFind));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithExactCategoryCreator(@NotNull LocalDate evaluationDate,
            @NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsMedicationOfExactCategory(evaluationDate, Sets.newHashSet(categoriesToFind));
    }
}
