package com.hartwig.actin.algo.evaluation.medication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class MedicationRuleMapping {

    private static final String ANTICOAGULANTS = "Anticoagulants";
    private static final String ANTIBIOTICS = "Antibiotics";
    private static final String ANTIEPILEPTICS = "Antiepileptics";
    private static final String CORTICOSTEROIDS = "Corticosteroids";
    private static final String VITAMIN_K_ANTAGONISTS = "Vitamin K Antagonists";
    private static final String NSAIDS = "NSAIDs";

    private MedicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ALLERGY_OF_NAME_X, function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.IS_ABLE_TO_SWALLOW_ORAL_MEDICATION, canSwallowOralMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY, getsAntiCancerMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_MEDICATION, getsActiveMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, getsActiveMedicationOfCategoryCreator(ANTIBIOTICS));
        map.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, getsActiveMedicationOfCategoryCreator(ANTICOAGULANTS));
        map.put(EligibilityRule.CURRENTLY_GETS_ANTIEPILEPTICS_MEDICATION, getsActiveMedicationOfCategoryCreator(ANTIEPILEPTICS));
        map.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, getsActiveMedicationOfCategoryCreator(CORTICOSTEROIDS));
        map.put(EligibilityRule.CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION,
                getsActiveMedicationOfCategoryCreator(VITAMIN_K_ANTAGONISTS));
        map.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, getsImmunoSuppressantMedicationCreator());
        map.put(EligibilityRule.CURRENTLY_GETS_NSAIDS_MEDICATION, getsActiveMedicationOfCategoryCreator(NSAIDS));
        map.put(EligibilityRule.CURRENTLY_GETS_PAIN_MEDICATION,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, currentlyGetsProhibitedMedicationCreator());
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
        return function -> new CurrentlyGetsMedicationWithCategory(null, false);
    }

    @NotNull
    private static FunctionCreator getsActiveMedicationOfCategoryCreator(@NotNull String category) {
        return function -> new CurrentlyGetsMedicationWithCategory(category, false);
    }

    @NotNull
    private static FunctionCreator getsImmunoSuppressantMedicationCreator() {
        return function -> new CurrentlyGetsImmunoSuppressantMedication();
    }

    @NotNull
    private static FunctionCreator currentlyGetsProhibitedMedicationCreator() {
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
    private static FunctionCreator getsActiveAndStableMedicationOfCategoryCreator(@NotNull String category) {
        return function -> new CurrentlyGetsMedicationWithCategory(category, true);
    }
}
