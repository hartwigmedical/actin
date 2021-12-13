package com.hartwig.actin.algo.evaluation.medication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class MedicationRuleMapping {

    private static final String ANTICOAGULANTS_TYPE = "Anticoagulants";
    private static final String ANTIBIOTICS_TYPE = "Antibiotics";
    private static final String CORTICOSTEROIDS_TYPE = "Corticosteroids";
    private static final String VITAMIN_K_ANTAGONISTS_TYPE = "Vitamin K Antagonists";

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.IS_ABLE_TO_SWALLOW_ORAL_MEDICATION, canSwallowOralMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY, getsAntiCancerMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION, getsActiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, getsActiveMedicationOfTypeCreator(ANTIBIOTICS_TYPE));
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsActiveMedicationOfTypeCreator(ANTICOAGULANTS_TYPE));
        map.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, getsActiveMedicationOfTypeCreator(CORTICOSTEROIDS_TYPE));
        map.put(EligibilityRule.CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION,
                getsActiveMedicationOfTypeCreator(VITAMIN_K_ANTAGONISTS_TYPE));
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, currentlyGetsImmunoSuppressantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, notImplementedCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, notImplementedCreator());
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, hasStableAnticoagulantDosingCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasAllergyRelatedToStudyMedicationCreator() {
        return function -> new HasAllergyRelatedToStudyMedication();
    }

    @NotNull
    private static FunctionCreator canSwallowOralMedicationCreator() {
        return function -> new CanSwallowOralMedication();
    }

    @NotNull
    private static FunctionCreator getsAntiCancerMedicationCreator() {
        return function -> new CurrentlyGetsAntiCancerMedication();
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationCreator() {
        return function -> new CurrentlyGetsMedicationWithType(null);
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationOfTypeCreator(@NotNull String type) {
        return function -> new CurrentlyGetsMedicationWithType(type);
    }

    @NotNull
    private static FunctionCreator currentlyGetsImmunoSuppressantMedicationCreator() {
        return function -> new CurrentlyGetsImmunoSuppressantMedication();
    }

    @NotNull
    private static FunctionCreator hasStableAnticoagulantDosingCreator() {
        return function -> new HasStableAnticoagulantDosing();
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
