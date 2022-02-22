package com.hartwig.actin.treatment.interpretation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.composite.CompositeInput;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;
import com.hartwig.actin.treatment.interpretation.single.FunctionInput;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneIntegerManyStrings;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneIntegerOneString;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneStringTwoIntegers;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneTreatmentCategoryOneInteger;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneTreatmentCategoryOneString;
import com.hartwig.actin.treatment.interpretation.single.ImmutableOneTreatmentCategoryOneStringOneInteger;
import com.hartwig.actin.treatment.interpretation.single.ImmutableTwoDoubles;
import com.hartwig.actin.treatment.interpretation.single.ImmutableTwoIntegers;
import com.hartwig.actin.treatment.interpretation.single.ImmutableTwoStrings;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerOneString;
import com.hartwig.actin.treatment.interpretation.single.OneStringTwoIntegers;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneInteger;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneString;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneStringOneInteger;
import com.hartwig.actin.treatment.interpretation.single.TwoDoubles;
import com.hartwig.actin.treatment.interpretation.single.TwoIntegers;
import com.hartwig.actin.treatment.interpretation.single.TwoStrings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FunctionInputResolver {

    private static final Logger LOGGER = LogManager.getLogger(FunctionInputResolver.class);

    private static final String MANY_STRING_SEPARATOR = ";";

    static final Map<EligibilityRule, FunctionInput> RULE_INPUT_MAP = Maps.newHashMap();

    static {
        RULE_INPUT_MAP.put(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.IS_MALE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PARTICIPATED_IN_CURRENT_TRIAL, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RAPIDLY_PROGRESSIVE_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.PATIENT_IS_TREATED_IN_HOSPITAL_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.PATIENT_WILL_BE_PARTICIPATING_IN_COUNTRY_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.PATIENT_IS_LEGALLY_INSTITUTIONALIZED, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_STAGE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ADVANCED_CANCER, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_METASTATIC_CANCER, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LIVER_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_CNS_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_BRAIN_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_BRAIN_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_BONE_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LUNG_METASTASES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_INJECTION_AMENABLE_LESION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_SUPERSCAN_BONE_SCAN, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, FunctionInput.ONE_TREATMENT_CATEGORY);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPE_Y, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES,
                FunctionInput.ONE_TREATMENT_CATEGORY_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES,
                FunctionInput.ONE_TREATMENT_CATEGORY_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_LEAST_Z_LINES,
                FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_MOST_Z_LINES,
                FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES,
                FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_FLUOROPYRIMIDINE_TREATMENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_TAXANE_TREATMENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_TYROSINE_KINASE_TREATMENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_INTRATUMURAL_INJECTION_TREATMENT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, FunctionInput.ONE_STRING);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X_CURRENTLY_INACTIVE,
                FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.EVERY_SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS, FunctionInput.ONE_INTEGER);

        RULE_INPUT_MAP.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE_FOR_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.INACTIVATION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, FunctionInput.TWO_STRINGS);
        RULE_INPUT_MAP.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.DELETION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.FUSION_IN_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y, FunctionInput.TWO_STRINGS);
        RULE_INPUT_MAP.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.NON_EXPRESSION_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_EXACTLY_Y, FunctionInput.ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y, FunctionInput.ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.WILDTYPE_OF_GENE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.MSI_SIGNATURE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HRD_SIGNATURE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.TMB_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.TML_OF_AT_LEAST_X, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.TML_OF_AT_MOST_X, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X, FunctionInput.ONE_INTEGER);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PTT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CALCIUM_MMOL_PER_L_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_SERUM_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TROPONIN_IT_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_STROKE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_TIA, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_GILBERT_DISEASE, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HYPERTENSION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_DIABETES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_IN_DIALYSIS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ADEQUATE_VEIN_ACCESS_FOR_LEUKAPHERESIS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA_OF_TYPE_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_QTCF_OF_AT_MOST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_LONG_QT_SYNDROME, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, FunctionInput.TWO_DOUBLES);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_ACTIVE_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_KNOWN_TUBERCOLOSIS_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_CURRENT_COVID_19_INFECTION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALLERGY_OF_NAME_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_PAIN_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_PROHIBITED_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_COLONY_STIMULATING_FACTORS, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_OATP_X, FunctionInput.ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_STABLE_PAIN_MEDICATION_DOSING, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_DRUG_X_CANCER_THERAPY_WITHIN_Y_WEEKS, FunctionInput.ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_CANCER_THERAPY_WITHIN_Y_WEEKS, FunctionInput.ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS,
                FunctionInput.ONE_STRING_ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES, FunctionInput.TWO_INTEGERS);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                FunctionInput.ONE_STRING_TWO_INTEGERS);
        RULE_INPUT_MAP.put(EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.IS_BREASTFEEDING, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.IS_PREGNANT, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.USES_ADEQUATE_ANTICONCEPTION, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_COMPLICATION_X, FunctionInput.ONE_STRING);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, FunctionInput.ONE_INTEGER_ONE_STRING);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, FunctionInput.ONE_INTEGER_MANY_STRINGS);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS, FunctionInput.NONE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_PULSE_OXYMETRY_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_BODY_WEIGHT_OF_AT_LEAST_X, FunctionInput.ONE_DOUBLE);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, FunctionInput.ONE_INTEGER);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, FunctionInput.ONE_INTEGER);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_RECENT_SURGERY, FunctionInput.NONE);
        RULE_INPUT_MAP.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, FunctionInput.ONE_INTEGER);

        RULE_INPUT_MAP.put(EligibilityRule.HAS_SMOKED_WITHIN_X_MONTHS, FunctionInput.ONE_INTEGER);
    }

    private FunctionInputResolver() {
    }

    @Nullable
    public static Boolean hasValidInputs(@NotNull EligibilityFunction function) {
        if (CompositeRules.isComposite(function.rule())) {
            return hasValidCompositeInputs(function);
        } else {
            return hasValidSingleInputs(function);
        }
    }

    private static boolean hasValidCompositeInputs(@NotNull EligibilityFunction function) {
        try {
            CompositeInput requiredInputs = CompositeRules.inputsForCompositeRule(function.rule());
            if (requiredInputs == CompositeInput.AT_LEAST_2) {
                createAtLeastTwoCompositeParameters(function);
            } else if (requiredInputs == CompositeInput.EXACTLY_1) {
                createOneCompositeParameter(function);
            } else {
                throw new IllegalStateException(
                        "Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Nullable
    private static Boolean hasValidSingleInputs(@NotNull EligibilityFunction function) {
        try {
            switch (RULE_INPUT_MAP.get(function.rule())) {
                case NONE: {
                    return function.parameters().isEmpty();
                }
                case ONE_INTEGER: {
                    createOneIntegerInput(function);
                    return true;
                }
                case TWO_INTEGERS: {
                    createTwoIntegerInput(function);
                    return true;
                }
                case ONE_DOUBLE: {
                    createOneDoubleInput(function);
                    return true;
                }
                case TWO_DOUBLES: {
                    createTwoDoubleInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY: {
                    createOneTreatmentCategoryInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_STRING: {
                    createOneTreatmentCategoryOneStringInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_INTEGER: {
                    createOneTreatmentCategoryOneIntegerInput(function);
                    return true;
                }
                case ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER: {
                    createOneTreatmentCategoryOneStringOneIntegerInput(function);
                    return true;
                }
                case ONE_STRING: {
                    createOneStringInput(function);
                    return true;
                }
                case ONE_STRING_ONE_INTEGER: {
                    createOneStringOneIntegerInput(function);
                    return true;
                }
                case ONE_STRING_TWO_INTEGERS: {
                    createOneStringTwoIntegerInput(function);
                    return true;
                }
                case TWO_STRINGS: {
                    createTwoStringInput(function);
                    return true;
                }
                case ONE_INTEGER_ONE_STRING: {
                    createOneIntegerOneStringInput(function);
                    return true;
                }
                case ONE_INTEGER_MANY_STRINGS: {
                    createOneIntegerManyStringsInput(function);
                    return true;
                }
                default: {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule());
                    return null;
                }
            }
        } catch (Exception exception) {
            return false;
        }
    }

    public static int createOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    @NotNull
    public static TwoIntegers createTwoIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_INTEGERS, 2);

        return ImmutableTwoIntegers.builder()
                .integer1(Integer.parseInt((String) function.parameters().get(0)))
                .integer2(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    public static double createOneDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_DOUBLE, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    @NotNull
    public static TwoDoubles createTwoDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_DOUBLES, 2);

        return ImmutableTwoDoubles.builder()
                .double1(Double.parseDouble((String) function.parameters().get(0)))
                .double2(Double.parseDouble((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static TreatmentCategory createOneTreatmentCategoryInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY, 1);

        return TreatmentCategoryResolver.fromString((String) function.parameters().get(0));
    }

    @NotNull
    public static OneTreatmentCategoryOneString createOneTreatmentCategoryOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING, 2);

        return ImmutableOneTreatmentCategoryOneString.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryOneInteger createOneTreatmentCategoryOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_INTEGER, 2);

        return ImmutableOneTreatmentCategoryOneInteger.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneTreatmentCategoryOneStringOneInteger createOneTreatmentCategoryOneStringOneIntegerInput(
            @NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER, 3);

        return ImmutableOneTreatmentCategoryOneStringOneInteger.builder()
                .treatmentCategory(TreatmentCategoryResolver.fromString((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .integer(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static String createOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public static OneIntegerOneString createOneStringOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING_ONE_INTEGER, 2);

        return ImmutableOneIntegerOneString.builder()
                .string((String) function.parameters().get(0))
                .integer(Integer.parseInt((String) function.parameters().get(1)))
                .build();
    }

    @NotNull
    public static OneStringTwoIntegers createOneStringTwoIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_STRING_TWO_INTEGERS, 3);

        return ImmutableOneStringTwoIntegers.builder()
                .string((String) function.parameters().get(0))
                .integer1(Integer.parseInt((String) function.parameters().get(1)))
                .integer2(Integer.parseInt((String) function.parameters().get(2)))
                .build();
    }

    @NotNull
    public static TwoStrings createTwoStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.TWO_STRINGS, 2);

        return ImmutableTwoStrings.builder()
                .string1((String) function.parameters().get(0))
                .string2((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerOneString createOneIntegerOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, FunctionInput.ONE_INTEGER_ONE_STRING, 2);

        return ImmutableOneIntegerOneString.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerManyStrings createOneIntegerManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamType(function, FunctionInput.ONE_INTEGER_MANY_STRINGS);

        List<String> strings = Lists.newArrayList();
        for (String input : ((String) function.parameters().get(1)).split(MANY_STRING_SEPARATOR)) {
            strings.add(input.trim());
        }

        return ImmutableOneIntegerManyStrings.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .strings(strings)
                .build();
    }

    @NotNull
    public static EligibilityFunction createOneCompositeParameter(@NotNull EligibilityFunction function) {
        assertParamCount(function, 1);

        return (EligibilityFunction) function.parameters().get(0);
    }

    @NotNull
    public static List<EligibilityFunction> createAtLeastTwoCompositeParameters(@NotNull EligibilityFunction function) {
        if (function.parameters().size() < 2) {
            throw new IllegalArgumentException(
                    "Not enough parameters passed into '" + function.rule() + "': " + function.parameters().size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : function.parameters()) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }

    private static void assertParamConfig(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput, int expectedCount) {
        assertParamType(function, requestedInput);
        assertParamCount(function, expectedCount);
    }

    private static void assertParamType(@NotNull EligibilityFunction function, @NotNull FunctionInput requestedInput) {
        if (requestedInput != RULE_INPUT_MAP.get(function.rule())) {
            throw new IllegalStateException("Incorrect type of inputs requested for '" + function.rule() + "': " + requestedInput);
        }
    }

    private static void assertParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size());
        }
    }
}
