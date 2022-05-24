package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;

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

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        LocalDate evaluationDate = referenceDateProvider.date();
        MedicationStatusInterpreter interpreter = new MedicationStatusInterpreterOnEvaluationDate(evaluationDate);
        MedicationSelector selector = new MedicationSelector(interpreter);

        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION, getsActiveMedicationWithConfiguredNameCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, getsActiveMedicationWithApproximateCategoryCreator(selector));
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS,
                hasRecentlyReceivedMedicationOfApproximateCategoryCreator(selector, evaluationDate));
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsAnticoagulantMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION, getsAzoleMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION, getsBoneResorptiveMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION, getsCoumarinDerivativeMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION, getsGonadorelinMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, getsImmunosuppressantMedicationCreator(selector));
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, getsProhibitedMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION, getsQTProlongatingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, getsCYPXInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, getsPGPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_OATP_X, getsOATPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP, getsBCRPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_DRUG_METABOLIZING_ENZYMES,
                getsDrugMetabolizingEnzymeInhibitingMedicationCreator(selector));
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, getsStableDosingAnticoagulantMedicationCreator(selector));
        map.put(EligibilityRule.IS_WILLING_TO_TAKE_PREMEDICATION, isWillingToTakePremedicationCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithConfiguredNameCreator(@NotNull MedicationSelector selector) {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsMedicationOfName(selector, Sets.newHashSet(termToFind));
        };
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithApproximateCategoryCreator(@NotNull MedicationSelector selector) {
        return function -> {
            String categoryTermToFind = FunctionInputResolver.createOneStringInput(function);
            return new CurrentlyGetsMedicationOfApproximateCategory(selector, categoryTermToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedMedicationOfApproximateCategoryCreator(@NotNull MedicationSelector selector,
            @NotNull LocalDate evaluationDate) {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate maxStopDate = evaluationDate.minusWeeks(input.integer());
            return new HasRecentlyReceivedMedicationOfApproximateCategory(selector, input.string(), maxStopDate);
        };
    }

    @NotNull
    private static FunctionCreator getsAnticoagulantMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsAzoleMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES);
    }

    @NotNull
    private static FunctionCreator getsBoneResorptiveMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION);
    }

    @NotNull
    private static FunctionCreator getsCoumarinDerivativeMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private static FunctionCreator getsGonadorelinMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS);
    }

    @NotNull
    private static FunctionCreator getsImmunosuppressantMedicationCreator(@NotNull MedicationSelector selector) {
        return getsActiveMedicationWithExactCategoryCreator(selector, SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS);
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
    private static FunctionCreator getsDrugMetabolizingEnzymeInhibitingMedicationCreator(@NotNull MedicationSelector selector) {
        return function -> new CurrentlyGetsMetabolizingEnzymeInhibitingMedication(selector);
    }

    @NotNull
    private static FunctionCreator getsStableDosingAnticoagulantMedicationCreator(@NotNull MedicationSelector selector) {
        return getsStableMedicationOfCategoryCreator(selector, ANTICOAGULANTS);
    }

    @NotNull
    private static FunctionCreator getsStableMedicationOfCategoryCreator(@NotNull MedicationSelector selector,
            @NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsStableMedicationOfCategory(selector, Sets.newHashSet(categoriesToFind));
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationWithExactCategoryCreator(@NotNull MedicationSelector selector,
            @NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsMedicationOfExactCategory(selector, Sets.newHashSet(categoriesToFind));
    }

    @NotNull
    private static FunctionCreator isWillingToTakePremedicationCreator() {
        return function -> new IsWillingToTakePremedication();
    }
}
