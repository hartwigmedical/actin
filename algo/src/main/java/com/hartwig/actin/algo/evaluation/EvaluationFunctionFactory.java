package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.bloodpressure.HasSufficientDBP;
import com.hartwig.actin.algo.evaluation.bloodpressure.HasSufficientSBP;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.HasHadRecentErythrocyteTransfusion;
import com.hartwig.actin.algo.evaluation.bloodtransfusion.HasHadRecentThrombocyteTransfusion;
import com.hartwig.actin.algo.evaluation.composite.And;
import com.hartwig.actin.algo.evaluation.composite.Not;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.algo.evaluation.composite.WarnOnPass;
import com.hartwig.actin.algo.evaluation.general.HasHadRecentSurgery;
import com.hartwig.actin.algo.evaluation.general.HasMaximumWHOStatus;
import com.hartwig.actin.algo.evaluation.general.HasSufficientLifeExpectancy;
import com.hartwig.actin.algo.evaluation.general.IsAtLeastEighteenYearsOld;
import com.hartwig.actin.algo.evaluation.general.IsBreastfeeding;
import com.hartwig.actin.algo.evaluation.general.IsPregnant;
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
import com.hartwig.actin.algo.evaluation.medication.HasToxicityWithGradeInNeuropathy;
import com.hartwig.actin.algo.evaluation.othercondition.HasActiveInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasGilbertDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasHistoryOfAutoimmuneDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasHistoryOfCardiacDisease;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHIVInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHepatitisBInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasKnownHepatitisCInfection;
import com.hartwig.actin.algo.evaluation.othercondition.HasSignificantConcomitantIllness;
import com.hartwig.actin.algo.evaluation.pathology.PrimaryTumorLocationBelongsToDoid;
import com.hartwig.actin.algo.evaluation.radiology.HasActiveCNSMetastases;
import com.hartwig.actin.algo.evaluation.radiology.HasAdvancedCancer;
import com.hartwig.actin.algo.evaluation.radiology.HasBiopsyAmenableLesion;
import com.hartwig.actin.algo.evaluation.radiology.HasLiverMetastases;
import com.hartwig.actin.algo.evaluation.radiology.HasMeasurableDiseaseRecist;
import com.hartwig.actin.algo.evaluation.radiology.HasMetastaticCancer;
import com.hartwig.actin.algo.evaluation.treatment.HasDeclinedSOCTreatments;
import com.hartwig.actin.algo.evaluation.treatment.HasExhaustedSOCTreatments;
import com.hartwig.actin.algo.evaluation.treatment.HasHadLimitedAntiPDL1OrPD1Immunotherapies;
import com.hartwig.actin.algo.evaluation.treatment.HasHistoryOfSecondMalignancy;
import com.hartwig.actin.algo.evaluation.treatment.SecondMalignancyHasBeenCuredRecently;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EvaluationFunctionFactory {

    private static final Logger LOGGER = LogManager.getLogger(EvaluationFunctionFactory.class);

    static final Map<EligibilityRule, FunctionCreator> FUNCTION_CREATOR_MAP = Maps.newHashMap();

    static {
        FUNCTION_CREATOR_MAP.put(EligibilityRule.AND, andCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.OR, orCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.NOT, notCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.WARN_ON_PASS, warnOnPassCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, isAtLeast18YearsOldCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, hasMaximumWHOStatusCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_GIVE_ADEQUATE_INFORMED_CONSENT, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_USE_ADEQUATE_ANTICONCEPTION, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_BREASTFEEDING, isBreastfeedingCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_PREGNANT, isPregnantCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, hasSufficientLifeExpectancyCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X, primaryTumorLocationBelongsToDoidCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ADVANCED_CANCER, hasAdvancedCancerCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_METASTATIC_CANCER, hasMetastaticCancerCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LIVER_METASTASES, hasLivesMetastasesCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CNS_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ACTIVE_CNS_METASTASES, hasActiveCNSMetastasesCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SYMPTOMATIC_CNS_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_BRAIN_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ACTIVE_BRAIN_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SYMPTOMATIC_BRAIN_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_BONE_METASTASES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, hasBiopsyAmenableLesionCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, hasDeclinedSOCTreatmentsCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS,
                secondMalignancyHasBeenCuredRecentlyCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_IMMUNOTHERAPY_TREATMENT, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES,
                hasHadLimitedAntiPDL1OrPD1ImmunotherapiesCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_STEM_CELL_TRANSPLANTATION, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientAbsLeukocytesCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_THROMBOCYTES_ABS_AT_LEAST_X, hasSufficientThrombocytesCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientHemoglobinCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasSufficientCreatinineCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCKDEPICreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_MDRD_OF_AT_LEAST_X, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X, hasLimitedTotalBilirubinCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X, hasLimitedDirectBilirubinCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_INR_ULN_AT_MOST_X, hasLimitedINRCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_PT_ULN_AT_MOST_X, hasLimitedPTCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_APTT_ULN_AT_MOST_X, hasLimitedAPPTCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedASATCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, hasSignificantConcomitantIllnessCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, hasHistoryOfAutoimmuneDiseaseCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, hasHistoryOfCardiacDiseaseCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_STROKE, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_TIA, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_GILBERT_DISEASE, hasGilbertDiseaseCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HYPERTENSION, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ACTIVE_INFECTION, hasActiveInfectionCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, hasKnownHepatitisBInfectionCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, hasKnownHepatitisCInfectionCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, hasKnownHIVInfectionCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, currentlyGetsCorticosteroidMedicationCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION,
                currentlyGetsImmunoSuppressantMedicationCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, hasStableAnticoagulantDosingCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, hasToxicityWithGradeCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_NEUROPATHY, hasToxicityWithGradeInNeuropathyCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_FATIGUE, notImplementedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_VITILIGO, notImplementedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, hasSufficientSBPCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, hasSufficientDBPCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentErythrocyteTransfusion());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentThrombocyteTransfusion());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadRecentSurgeryCreator());
    }

    private EvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction create(@NotNull EligibilityFunction function) {
        Boolean hasValidParameters = EligibilityParameterResolver.hasValidParameters(function);
        if (hasValidParameters == null || !hasValidParameters) {
            LOGGER.warn("Function with rule '{}' has invalid inputs {}. Evaluation for this rule will always be undetermined",
                    function.rule(),
                    function.parameters());
            return cannotBeDeterminedCreator().create(function);
        }

        return FUNCTION_CREATOR_MAP.get(function.rule()).create(function);
    }

    @NotNull
    private static FunctionCreator andCreator() {
        return function -> new And(createMultipleCompositeParameters(function));
    }

    @NotNull
    private static FunctionCreator orCreator() {
        return function -> new Or(createMultipleCompositeParameters(function));
    }

    @NotNull
    private static FunctionCreator notCreator() {
        return function -> new Not(createSingleCompositeParameter(function));
    }

    @NotNull
    private static FunctionCreator warnOnPassCreator() {
        return function -> new WarnOnPass(createSingleCompositeParameter(function));
    }

    @NotNull
    private static FunctionCreator hasHadRecentSurgeryCreator() {
        return function -> new HasHadRecentSurgery();
    }

    @NotNull
    private static FunctionCreator hasMaximumWHOStatusCreator() {
        return function -> new HasMaximumWHOStatus();
    }

    @NotNull
    private static FunctionCreator hasSufficientLifeExpectancyCreator() {
        return function -> new HasSufficientLifeExpectancy();
    }

    @NotNull
    private static FunctionCreator isAtLeast18YearsOldCreator() {
        return function -> new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
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
    private static FunctionCreator primaryTumorLocationBelongsToDoidCreator() {
        return function -> new PrimaryTumorLocationBelongsToDoid();
    }

    @NotNull
    private static FunctionCreator hasActiveCNSMetastasesCreator() {
        return function -> new HasActiveCNSMetastases();
    }

    @NotNull
    private static FunctionCreator hasAdvancedCancerCreator() {
        return function -> new HasAdvancedCancer();
    }

    @NotNull
    private static FunctionCreator hasBiopsyAmenableLesionCreator() {
        return function -> new HasBiopsyAmenableLesion();
    }

    @NotNull
    private static FunctionCreator hasLivesMetastasesCreator() {
        return function -> new HasLiverMetastases();
    }

    @NotNull
    private static EvaluationFunction createSingleCompositeParameter(@NotNull EligibilityFunction function) {
        return create(EligibilityParameterResolver.createSingleCompositeParameter(function));
    }

    @NotNull
    private static FunctionCreator hasMeasurableDiseaseRecistCreator() {
        return function -> new HasMeasurableDiseaseRecist();
    }

    @NotNull
    private static FunctionCreator hasMetastaticCancerCreator() {
        return function -> new HasMetastaticCancer();
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
    private static FunctionCreator hasToxicityWithGradeInNeuropathyCreator() {
        return function -> new HasToxicityWithGradeInNeuropathy();
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
    private static List<EvaluationFunction> createMultipleCompositeParameters(@NotNull EligibilityFunction function) {
        List<EvaluationFunction> parameters = Lists.newArrayList();
        for (EligibilityFunction input : EligibilityParameterResolver.createAtLeastTwoCompositeParameters(function)) {
            parameters.add(create(input));
        }
        return parameters;
    }

    @NotNull
    private static FunctionCreator cannotBeDeterminedCreator() {
        return function -> evaluation -> Evaluation.UNDETERMINED;
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }

    private interface FunctionCreator {

        @NotNull
        EvaluationFunction create(@NotNull EligibilityFunction function);
    }
}
