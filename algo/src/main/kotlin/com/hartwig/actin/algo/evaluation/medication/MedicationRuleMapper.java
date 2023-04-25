package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;

import org.jetbrains.annotations.NotNull;

public class MedicationRuleMapper extends RuleMapper {

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

    @NotNull
    private final MedicationSelector selector;

    public MedicationRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);

        MedicationStatusInterpreter interpreter = new MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date());
        this.selector = new MedicationSelector(interpreter);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION, getsActiveMedicationWithConfiguredNameCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, getsActiveMedicationWithApproximateCategoryCreator());
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS,
                hasRecentlyReceivedMedicationOfApproximateCategoryCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsAnticoagulantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION, getsAzoleMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION, getsBoneResorptiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION, getsCoumarinDerivativeMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION, getsGonadorelinMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, getsImmunosuppressantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, getsProhibitedMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION, getsQTProlongatingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP, getsAnyCYPInducingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X, getsCYPXInducingMedicationCreator());
        map.put(EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS, hasRecentlyReceivedCYPXInducingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X, getsCYPXInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, getsCYPXInhibitingOrInducingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X, getsCYPSubstrateMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, getsPGPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_PGP, getsPGPSubstrateMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP, getsBCRPInhibitingMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP, getsBCRPSubstrateMedicationCreator());
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, getsStableDosingAnticoagulantMedicationCreator());

        return map;
    }

    @NotNull
    private FunctionCreator getsActiveMedicationWithConfiguredNameCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsMedicationOfName(selector, Sets.newHashSet(termToFind));
        };
    }

    @NotNull
    private FunctionCreator getsActiveMedicationWithApproximateCategoryCreator() {
        return function -> {
            String categoryTermToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsMedicationOfApproximateCategory(selector, categoryTermToFind);
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedMedicationOfApproximateCategoryCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            LocalDate maxStopDate = referenceDateProvider().date().minusWeeks(input.integer());
            return new HasRecentlyReceivedMedicationOfApproximateCategory(selector, input.string(), maxStopDate);
        };
    }

    @NotNull
    private FunctionCreator getsAnticoagulantMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private FunctionCreator getsAzoleMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES);
    }

    @NotNull
    private FunctionCreator getsBoneResorptiveMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION);
    }

    @NotNull
    private FunctionCreator getsCoumarinDerivativeMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(VITAMIN_K_ANTAGONISTS);
    }

    @NotNull
    private FunctionCreator getsGonadorelinMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS);
    }

    @NotNull
    private FunctionCreator getsImmunosuppressantMedicationCreator() {
        return getsActiveMedicationWithExactCategoryCreator(SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS);
    }

    @NotNull
    private FunctionCreator getsProhibitedMedicationCreator() {
        return function -> new CurrentlyGetsProhibitedMedicationCreator();
    }

    @NotNull
    private FunctionCreator getsQTProlongatingMedicationCreator() {
        return function -> new CurrentlyGetsQTProlongatingMedication();
    }

    @NotNull
    private FunctionCreator getsAnyCYPInducingMedicationCreator() {
        return function -> new GetsAnyCYPInducingMedication();
    }

    @NotNull
    private FunctionCreator getsCYPXInducingMedicationCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsCYPXInducingMedication(termToFind);
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedCYPXInducingMedicationCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new HasRecentlyReceivedCYPXInducingMedication(input.string());
        };
    }

    @NotNull
    private FunctionCreator getsCYPXInhibitingMedicationCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsCYPXInhibitingMedication(termToFind);
        };
    }

    @NotNull
    private FunctionCreator getsCYPXInhibitingOrInducingMedicationCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsCYPXInhibitingOrInducingMedication(termToFind);
        };
    }

    @NotNull
    private FunctionCreator getsCYPSubstrateMedicationCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new CurrentlyGetsCYPXSubstrateMedication(termToFind);
        };
    }

    @NotNull
    private FunctionCreator getsPGPInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsPGPInhibitingMedication();
    }

    @NotNull
    private FunctionCreator getsPGPSubstrateMedicationCreator() {
        return function -> new CurrentlyGetsPGPSubstrateMedication();
    }

    @NotNull
    private FunctionCreator getsBCRPInhibitingMedicationCreator() {
        return function -> new CurrentlyGetsBCRPInhibitingMedication();
    }

    @NotNull
    private FunctionCreator getsBCRPSubstrateMedicationCreator() {
        return function -> new CurrentlyGetsBCRPSubstrateMedication();
    }

    @NotNull
    private FunctionCreator getsStableDosingAnticoagulantMedicationCreator() {
        return getsStableMedicationOfCategoryCreator(ANTICOAGULANTS);
    }

    @NotNull
    private FunctionCreator getsStableMedicationOfCategoryCreator(@NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsStableMedicationOfCategory(selector, Sets.newHashSet(categoriesToFind));
    }

    @NotNull
    private FunctionCreator getsActiveMedicationWithExactCategoryCreator(@NotNull String... categoriesToFind) {
        return function -> new CurrentlyGetsMedicationOfExactCategory(selector, Sets.newHashSet(categoriesToFind));
    }
}
