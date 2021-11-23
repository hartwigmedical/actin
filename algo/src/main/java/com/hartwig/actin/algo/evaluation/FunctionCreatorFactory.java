package com.hartwig.actin.algo.evaluation;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.bloodpressure.HasSufficientDBP;
import com.hartwig.actin.algo.evaluation.bloodpressure.HasSufficientSBP;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.HasHadRecentErythrocyteTransfusion;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.HasHadRecentThrombocyteTransfusion;
import com.hartwig.actin.algo.evaluation.general.GeneralRuleMapping;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedAPTT;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedASAT;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedDirectBilirubin;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedINR;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedPT;
import com.hartwig.actin.algo.evaluation.laboratory.HasLimitedTotalBilirubin;
import com.hartwig.actin.algo.evaluation.laboratory.HasSufficientAbsLeukocytes;
import com.hartwig.actin.algo.evaluation.laboratory.HasSufficientCreatinine;
import com.hartwig.actin.algo.evaluation.laboratory.HasSufficientCreatinineClearanceCKDEPI;
import com.hartwig.actin.algo.evaluation.laboratory.HasSufficientHemoglobin;
import com.hartwig.actin.algo.evaluation.laboratory.HasSufficientThrombocytes;
import com.hartwig.actin.algo.evaluation.medication.CurrentlyGetsCorticosteroidMedication;
import com.hartwig.actin.algo.evaluation.medication.CurrentlyGetsImmunoSuppressantMedication;
import com.hartwig.actin.algo.evaluation.medication.HasAllergyRelatedToStudyMedication;
import com.hartwig.actin.algo.evaluation.medication.HasStableAnticoagulantDosing;
import com.hartwig.actin.algo.evaluation.medication.HasToxicityWithGrade;
import com.hartwig.actin.algo.evaluation.othercondition.HasActiveInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasGilbertDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasHistoryOfAutoimmuneDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasHistoryOfCardiacDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHIVInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHepatitisBInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHepatitisCInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasSignificantConcomitantIllness;
import com.hartwig.actin.algo.evaluation.pregnancy.IsBreastfeeding;
import com.hartwig.actin.algo.evaluation.pregnancy.IsPregnant;
import com.hartwig.actin.algo.evaluation.surgery.HasHadRecentSurgery;
import com.hartwig.actin.algo.evaluation.treatment.HasDeclinedSOCTreatments;
import com.hartwig.actin.algo.evaluation.treatment.HasExhaustedSOCTreatments;
import com.hartwig.actin.algo.evaluation.treatment.HasHadLimitedAntiPDL1OrPD1Immunotherapies;
import com.hartwig.actin.algo.evaluation.treatment.HasHistoryOfSecondMalignancy;
import com.hartwig.actin.algo.evaluation.treatment.SecondMalignancyHasBeenCuredRecently;
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapping;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

final class FunctionCreatorFactory {

    private FunctionCreatorFactory() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> createFunctionCreatorMap(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> functionCreatorMap = Maps.newHashMap();

        functionCreatorMap.putAll(GeneralRuleMapping.create());
        functionCreatorMap.putAll(TumorRuleMapping.create(doidModel));

        functionCreatorMap.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        functionCreatorMap.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, hasDeclinedSOCTreatmentsCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator());
        functionCreatorMap.put(EligibilityRule.SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS,
                secondMalignancyHasBeenCuredRecentlyCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HAD_IMMUNOTHERAPY_TREATMENT, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES,
                hasHadLimitedAntiPDL1OrPD1ImmunotherapiesCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HAD_STEM_CELL_TRANSPLANTATION, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.ACTIVATION_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.INACTIVATION_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.MUTATION_IN_GENE_X_IS_SPECIFIC_MUTATION_Y, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.DELETION_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.SPECIFIC_FUSION_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.WILDTYPE_OF_GENE_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.MSI_SIGNATURE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HRD_SIGNATURE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.TMB_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.TML_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.TML_OF_AT_MOST_X, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientAbsLeukocytesCreator());
        functionCreatorMap.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_THROMBOCYTES_ABS_AT_LEAST_X, hasSufficientThrombocytesCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientHemoglobinCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasSufficientCreatinineCreator());
        functionCreatorMap.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCKDEPICreator());
        functionCreatorMap.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X, hasLimitedTotalBilirubinCreator());
        functionCreatorMap.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X, hasLimitedDirectBilirubinCreator());
        functionCreatorMap.put(EligibilityRule.HAS_INR_ULN_AT_MOST_X, hasLimitedINRCreator());
        functionCreatorMap.put(EligibilityRule.HAS_PT_ULN_AT_MOST_X, hasLimitedPTCreator());
        functionCreatorMap.put(EligibilityRule.HAS_APTT_ULN_AT_MOST_X, hasLimitedAPPTCreator());
        functionCreatorMap.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedASATCreator());
        functionCreatorMap.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, hasSignificantConcomitantIllnessCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasHistoryOfAutoimmuneDiseaseCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasHistoryOfCardiacDiseaseCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_STROKE, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HISTORY_OF_TIA, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_GILBERT_DISEASE, hasGilbertDiseaseCreator());
        functionCreatorMap.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_HYPERTENSION, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_KNOWN_LVEF_OF_AT_MOST_X, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.HAS_ACTIVE_INFECTION, hasActiveInfectionCreator());
        functionCreatorMap.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, hasKnownHepatitisBInfectionCreator());
        functionCreatorMap.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, hasKnownHepatitisCInfectionCreator());
        functionCreatorMap.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, hasKnownHIVInfectionCreator());

        functionCreatorMap.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        functionCreatorMap.put(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, currentlyGetsCorticosteroidMedicationCreator());
        functionCreatorMap.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION,
                currentlyGetsImmunoSuppressantMedicationCreator());
        functionCreatorMap.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, hasStableAnticoagulantDosingCreator());

        functionCreatorMap.put(EligibilityRule.IS_BREASTFEEDING, isBreastfeedingCreator());
        functionCreatorMap.put(EligibilityRule.IS_PREGNANT, isPregnantCreator());
        functionCreatorMap.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_USE_ADEQUATE_ANTICONCEPTION, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, hasToxicityWithGradeCreator());
        functionCreatorMap.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, notImplementedCreator());
        functionCreatorMap.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, notImplementedCreator());

        functionCreatorMap.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, hasSufficientSBPCreator());
        functionCreatorMap.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, hasSufficientDBPCreator());

        functionCreatorMap.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentErythrocyteTransfusion());
        functionCreatorMap.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentThrombocyteTransfusion());

        functionCreatorMap.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadRecentSurgeryCreator());

        return functionCreatorMap;
    }

    @NotNull
    private static FunctionCreator hasDeclinedSOCTreatmentsCreator() {
        return function -> new HasDeclinedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator hasExhaustedSOCTreatmentsCreator() {
        return function -> new HasExhaustedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator hasHadLimitedAntiPDL1OrPD1ImmunotherapiesCreator() {
        return function -> new HasHadLimitedAntiPDL1OrPD1Immunotherapies();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyCreator() {
        return function -> new HasHistoryOfSecondMalignancy();
    }

    @NotNull
    private static FunctionCreator secondMalignancyHasBeenCuredRecentlyCreator() {
        return function -> new SecondMalignancyHasBeenCuredRecently();
    }

    @NotNull
    private static FunctionCreator hasLimitedAPPTCreator() {
        return function -> new HasLimitedAPTT();
    }

    @NotNull
    private static FunctionCreator hasLimitedASATCreator() {
        return function -> new HasLimitedASAT();
    }

    @NotNull
    private static FunctionCreator hasLimitedINRCreator() {
        return function -> new HasLimitedINR();
    }

    @NotNull
    private static FunctionCreator hasLimitedPTCreator() {
        return function -> new HasLimitedPT();
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsLeukocytesCreator() {
        return function -> new HasSufficientAbsLeukocytes();
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineCreator() {
        return function -> new HasSufficientCreatinine();
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCKDEPICreator() {
        return function -> new HasSufficientCreatinineClearanceCKDEPI();
    }

    @NotNull
    private static FunctionCreator hasLimitedDirectBilirubinCreator() {
        return function -> new HasLimitedDirectBilirubin();
    }

    @NotNull
    private static FunctionCreator hasSufficientHemoglobinCreator() {
        return function -> new HasSufficientHemoglobin();
    }

    @NotNull
    private static FunctionCreator hasSufficientThrombocytesCreator() {
        return function -> new HasSufficientThrombocytes();
    }

    @NotNull
    private static FunctionCreator hasLimitedTotalBilirubinCreator() {
        return function -> new HasLimitedTotalBilirubin();
    }

    @NotNull
    private static FunctionCreator hasActiveInfectionCreator() {
        return function -> new HasActiveInfection();
    }

    @NotNull
    private static FunctionCreator hasGilbertDiseaseCreator() {
        return function -> new HasGilbertDisease();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfAutoimmuneDiseaseCreator() {
        return function -> new HasHistoryOfAutoimmuneDisease();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfCardiacDiseaseCreator() {
        return function -> new HasHistoryOfCardiacDisease();
    }

    @NotNull
    private static FunctionCreator hasKnownHepatitisBInfectionCreator() {
        return function -> new HasKnownHepatitisBInfection();
    }

    @NotNull
    private static FunctionCreator hasKnownHepatitisCInfectionCreator() {
        return function -> new HasKnownHepatitisCInfection();
    }

    @NotNull
    private static FunctionCreator hasKnownHIVInfectionCreator() {
        return function -> new HasKnownHIVInfection();
    }

    @NotNull
    private static FunctionCreator hasSignificantConcomitantIllnessCreator() {
        return function -> new HasSignificantConcomitantIllness();
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
    private static FunctionCreator hasAllergyRelatedToStudyMedicationCreator() {
        return function -> new HasAllergyRelatedToStudyMedication();
    }

    @NotNull
    private static FunctionCreator hasStableAnticoagulantDosingCreator() {
        return function -> new HasStableAnticoagulantDosing();
    }

    @NotNull
    private static FunctionCreator hasToxicityWithGradeCreator() {
        return function -> new HasToxicityWithGrade();
    }

    @NotNull
    private static FunctionCreator isBreastfeedingCreator() {
        return function -> new IsBreastfeeding();
    }

    @NotNull
    private static FunctionCreator isPregnantCreator() {
        return function -> new IsPregnant();
    }

    @NotNull
    private static FunctionCreator hasSufficientDBPCreator() {
        return function -> new HasSufficientDBP();
    }

    @NotNull
    private static FunctionCreator hasSufficientSBPCreator() {
        return function -> new HasSufficientSBP();
    }

    @NotNull
    private static FunctionCreator hasHadRecentErythrocyteTransfusion() {
        return function -> new HasHadRecentErythrocyteTransfusion();
    }

    @NotNull
    private static FunctionCreator hasHadRecentThrombocyteTransfusion() {
        return function -> new HasHadRecentThrombocyteTransfusion();
    }

    @NotNull
    private static FunctionCreator hasHadRecentSurgeryCreator() {
        return function -> new HasHadRecentSurgery();
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
