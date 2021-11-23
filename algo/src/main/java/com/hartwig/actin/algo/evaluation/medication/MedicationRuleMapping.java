package com.hartwig.actin.algo.evaluation.medication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class MedicationRuleMapping {

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY, notImplementedCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, notImplementedCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, currentlyGetsCorticosteroidMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, currentlyGetsImmunoSuppressantMedicationCreator());
        map.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, hasStableAnticoagulantDosingCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasAllergyRelatedToStudyMedicationCreator() {
        return function -> new HasAllergyRelatedToStudyMedication();
    }

    @NotNull
    private static FunctionCreator currentlyGetsCorticosteroidMedicationCreator() {
        return function -> new CurrentlyGetsCorticosteroidMedication();
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
