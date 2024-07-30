package com.hartwig.actin.trial.datamodel

import com.hartwig.actin.trial.input.single.FunctionInput

enum class EligibilityRule(val input: FunctionInput?) {

    // Composite rules combining other rules.
    AND(null),
    OR(null),
    NOT(null),
    WARN_IF(null),

    // Rules related to general patient characteristics / statements
    IS_AT_LEAST_X_YEARS_OLD(FunctionInput.ONE_INTEGER),
    IS_MALE(FunctionInput.NONE),
    IS_FEMALE(FunctionInput.NONE),
    HAS_WHO_STATUS_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),
    HAS_WHO_STATUS_OF_AT_EXACTLY_X(FunctionInput.ONE_INTEGER),
    HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    HAS_LANSKY_SCORE_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    CAN_GIVE_ADEQUATE_INFORMED_CONSENT(FunctionInput.NONE),
    HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS(FunctionInput.ONE_INTEGER),
    WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X(FunctionInput.ONE_STRING),
    IS_LEGALLY_INSTITUTIONALIZED(FunctionInput.NONE),
    IS_INVOLVED_IN_STUDY_PROCEDURES(FunctionInput.NONE),
    USES_TOBACCO_PRODUCTS(FunctionInput.NONE),

    // Rules related to tumor and lesion localization
    HAS_SOLID_PRIMARY_TUMOR(FunctionInput.NONE),
    HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA(FunctionInput.NONE),
    HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X(FunctionInput.ONE_DOID_TERM),
    HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X_DISTAL_SUB_LOCATION(FunctionInput.ONE_DOID_TERM),
    HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X(FunctionInput.ONE_TUMOR_TYPE),
    HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT(FunctionInput.NONE),
    HAS_CANCER_WITH_SMALL_CELL_COMPONENT(FunctionInput.NONE),
    HAS_KNOWN_SCLC_TRANSFORMATION(FunctionInput.NONE),
    HAS_NON_SQUAMOUS_NSCLC(FunctionInput.NONE),
    HAS_BREAST_CANCER_RECEPTOR_X_POSITIVE(FunctionInput.ONE_RECEPTOR_TYPE),
    HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT(FunctionInput.NONE),
    HAS_OVARIAN_BORDERLINE_TUMOR(FunctionInput.NONE),
    HAS_STOMACH_UNDIFFERENTIATED_TUMOR(FunctionInput.NONE),
    HAS_SECONDARY_GLIOBLASTOMA(FunctionInput.NONE),
    HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE(FunctionInput.NONE),
    HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE(FunctionInput.NONE),
    HAS_ANY_STAGE_X(FunctionInput.MANY_TUMOR_STAGES),
    HAS_LOCALLY_ADVANCED_CANCER(FunctionInput.NONE),
    HAS_METASTATIC_CANCER(FunctionInput.NONE),
    HAS_UNRESECTABLE_CANCER(FunctionInput.NONE),
    HAS_UNRESECTABLE_STAGE_III_CANCER(FunctionInput.NONE),
    HAS_RECURRENT_CANCER(FunctionInput.NONE),
    HAS_INCURABLE_CANCER(FunctionInput.NONE),
    HAS_ANY_LESION(FunctionInput.NONE),
    HAS_LIVER_METASTASES(FunctionInput.NONE),
    HAS_LIVER_METASTASES_ONLY(FunctionInput.NONE),
    MEETS_SPECIFIC_CRITERIA_REGARDING_LIVER_METASTASES(FunctionInput.NONE),
    HAS_KNOWN_CNS_METASTASES(FunctionInput.NONE),
    HAS_KNOWN_ACTIVE_CNS_METASTASES(FunctionInput.NONE),
    HAS_KNOWN_BRAIN_METASTASES(FunctionInput.NONE),
    HAS_KNOWN_ACTIVE_BRAIN_METASTASES(FunctionInput.NONE),
    MEETS_SPECIFIC_CRITERIA_REGARDING_BRAIN_METASTASES(FunctionInput.NONE),
    HAS_EXTRACRANIAL_METASTASES(FunctionInput.NONE),
    HAS_BONE_METASTASES(FunctionInput.NONE),
    HAS_BONE_METASTASES_ONLY(FunctionInput.NONE),
    HAS_LUNG_METASTASES(FunctionInput.NONE),
    HAS_LYMPH_NODE_METASTASES(FunctionInput.NONE),
    HAS_VISCERAL_METASTASES(FunctionInput.NONE),
    HAS_UNRESECTABLE_PERITONEAL_METASTASES(FunctionInput.NONE),
    HAS_EXTENSIVE_SYSTEMIC_METASTASES_PREDOMINANTLY_DETERMINING_PROGNOSIS(FunctionInput.NONE),
    HAS_BIOPSY_AMENABLE_LESION(FunctionInput.NONE),
    HAS_IRRADIATION_AMENABLE_LESION(FunctionInput.NONE),
    HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES(FunctionInput.ONE_INTEGER),
    CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS(FunctionInput.NONE),
    CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS(FunctionInput.NONE),
    MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY(FunctionInput.NONE),
    HAS_EVALUABLE_DISEASE(FunctionInput.NONE),
    HAS_MEASURABLE_DISEASE(FunctionInput.NONE),
    HAS_MEASURABLE_DISEASE_RECIST(FunctionInput.NONE),
    HAS_MEASURABLE_DISEASE_RANO(FunctionInput.NONE),
    HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA(FunctionInput.NONE),
    HAS_INJECTION_AMENABLE_LESION(FunctionInput.NONE),
    HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION(FunctionInput.NONE),
    HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI(FunctionInput.NONE),
    HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI(FunctionInput.NONE),
    HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT(FunctionInput.NONE),
    HAS_SUPERSCAN_BONE_SCAN(FunctionInput.NONE),
    HAS_BCLC_STAGE_X(FunctionInput.ONE_STRING),
    HAS_LEFT_SIDED_COLORECTAL_TUMOR(FunctionInput.NONE),
    HAS_SYMPTOMS_OF_PRIMARY_TUMOR_IN_SITU(FunctionInput.NONE),

    // Rules related to previous cancer treatments
    IS_NOT_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT(FunctionInput.NONE),
    IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X(FunctionInput.ONE_SPECIFIC_TREATMENT),
    IS_ELIGIBLE_FOR_PALLIATIVE_RADIOTHERAPY(FunctionInput.NONE),
    IS_ELIGIBLE_FOR_LOCO_REGIONAL_THERAPY(FunctionInput.NONE),
    IS_ELIGIBLE_FOR_TREATMENT_LINES_X(FunctionInput.MANY_INTEGERS),
    IS_ELIGIBLE_FOR_LOCAL_LIVER_TREATMENT(FunctionInput.NONE),
    IS_ELIGIBLE_FOR_INTENSIVE_TREATMENT(FunctionInput.NONE),
    HAS_EXHAUSTED_SOC_TREATMENTS(FunctionInput.NONE),
    HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES(FunctionInput.ONE_INTEGER),
    HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES(FunctionInput.ONE_INTEGER),
    HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES(FunctionInput.ONE_INTEGER),
    HAS_HAD_ANY_CANCER_TREATMENT(FunctionInput.NONE),
    HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE),
    HAS_NOT_RECEIVED_ANY_CANCER_TREATMENT_WITHIN_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_HAD_TREATMENT_NAME_X(FunctionInput.ONE_SPECIFIC_TREATMENT),
    HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS(FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER),
    HAS_HAD_FIRST_LINE_TREATMENT_NAME_X(FunctionInput.ONE_SPECIFIC_TREATMENT),
    HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z(FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_TREATMENT_WITH_ANY_DRUG_X(FunctionInput.MANY_DRUGS),
    HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_AS_MOST_RECENT_LINE(FunctionInput.MANY_DRUGS),
    HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS(FunctionInput.MANY_STRINGS_ONE_INTEGER),
    HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES(FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS),
    HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_WITHIN_Y_WEEKS(FunctionInput.MANY_DRUGS_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_RECEIVED_PLATINUM_BASED_DOUBLET(FunctionInput.NONE),
    HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y_WITHIN_Z_WEEKS(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_IGNORING_DRUGS_Z(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS),
    HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITH_STOP_REASON_OTHER_THAN_PD(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_FOR_AT_MOST_Z_WEEKS_WITH_STOP_REASON_OTHER_THAN_PD(
        FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER
    ),
    HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE),
    HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT_WITHIN_Y_WEEKS(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER),
    HAS_HAD_SYSTEMIC_THERAPY_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_WEEKS(FunctionInput.MANY_INTENTS_ONE_INTEGER),
    HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X(FunctionInput.MANY_INTENTS),
    HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_NAME_X_TREATMENT(FunctionInput.ONE_SPECIFIC_TREATMENT),
    HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE),
    HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC(FunctionInput.NONE),
    HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC_EXCLUDING_DRIVER_GENES_X(FunctionInput.MANY_GENES),
    HAS_HAD_TARGETED_THERAPY_INTERFERING_WITH_RAS_MEK_MAPK_PATHWAY(FunctionInput.NONE),
    HAS_HAD_NON_INTERNAL_RADIOTHERAPY(FunctionInput.NONE),
    HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X(FunctionInput.ONE_STRING),
    HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X_WITHIN_Y_WEEKS(FunctionInput.ONE_STRING_ONE_INTEGER),
    HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT(FunctionInput.ONE_SPECIFIC_TREATMENT),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT(FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y(FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS(
        FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER
    ),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES(
        FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER
    ),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES(FunctionInput.ONE_INTEGER),
    HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X(FunctionInput.MANY_DRUGS),
    HAS_ACQUIRED_RESISTANCE_TO_ANY_DRUG_X(FunctionInput.MANY_DRUGS),
    HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES(FunctionInput.ONE_INTEGER),
    HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE(FunctionInput.NONE),
    HAS_HAD_COMPLETE_RESECTION(FunctionInput.NONE),
    HAS_HAD_PARTIAL_RESECTION(FunctionInput.NONE),
    HAS_HAD_RESECTION_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_HAD_LIVER_RESECTION(FunctionInput.NONE),
    HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT(FunctionInput.NONE),
    HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT(FunctionInput.ONE_DOUBLE),
    HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL(FunctionInput.NONE),
    HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL_X(FunctionInput.ONE_STRING),
    IS_NOT_PARTICIPATING_IN_ANOTHER_TRIAL(FunctionInput.NONE),
    HAS_RECEIVED_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES(FunctionInput.NONE),
    HAS_HAD_BRAIN_RADIATION_THERAPY(FunctionInput.NONE),

    // Rules related to previous primary tumors
    HAS_ACTIVE_SECOND_MALIGNANCY(FunctionInput.NONE),
    HAS_HISTORY_OF_SECOND_MALIGNANCY(FunctionInput.NONE),
    HAS_HISTORY_OF_SECOND_MALIGNANCY_IGNORING_DOID_TERMS_X(FunctionInput.MANY_DOID_TERMS),
    HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_TERM_X(FunctionInput.ONE_DOID_TERM),
    HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS(FunctionInput.ONE_INTEGER),
    HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS_IGNORING_DOID_TERMS_Y(FunctionInput.ONE_INTEGER_MANY_DOID_TERMS),

    // Rules related to molecular results
    DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE(FunctionInput.MANY_GENES),
    HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC(FunctionInput.NONE),
    HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC_EXCLUDING_ANY_GENE_X(FunctionInput.MANY_GENES),
    ACTIVATION_OR_AMPLIFICATION_OF_GENE_X(FunctionInput.ONE_GENE),
    INACTIVATION_OF_GENE_X(FunctionInput.ONE_GENE),
    ACTIVATING_MUTATION_IN_ANY_GENES_X(FunctionInput.MANY_GENES),
    ACTIVATING_MUTATION_IN_GENE_X_EXCLUDING_CODONS_Y(FunctionInput.ONE_GENE_MANY_CODONS),
    MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y(FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS),
    MUTATION_IN_GENE_X_IN_ANY_CODONS_Y(FunctionInput.ONE_GENE_MANY_CODONS),
    MUTATION_IN_GENE_X_IN_EXON_Y(FunctionInput.ONE_GENE_ONE_INTEGER),
    MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z(FunctionInput.ONE_GENE_TWO_INTEGERS),
    MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z(FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE),
    UTR_3_LOSS_IN_GENE_X(FunctionInput.ONE_GENE),
    AMPLIFICATION_OF_GENE_X(FunctionInput.ONE_GENE),
    AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES(FunctionInput.ONE_GENE_ONE_INTEGER),
    FUSION_IN_GENE_X(FunctionInput.ONE_GENE),
    WILDTYPE_OF_GENE_X(FunctionInput.ONE_GENE),
    EXON_SKIPPING_GENE_X_EXON_Y(FunctionInput.ONE_GENE_ONE_INTEGER),
    MSI_SIGNATURE(FunctionInput.NONE),
    HRD_SIGNATURE(FunctionInput.NONE),
    HRD_SIGNATURE_WITHOUT_MUTATION_OR_WITH_VUS_MUTATION_IN_GENES_X(FunctionInput.MANY_GENES),
    HRD_SIGNATURE_WITHOUT_MUTATION_IN_GENES_X(FunctionInput.MANY_GENES),
    TMB_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    TML_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    TML_BETWEEN_X_AND_Y(FunctionInput.TWO_INTEGERS),
    HAS_HLA_TYPE_X(FunctionInput.ONE_HLA_ALLELE),
    HAS_UGT1A1_HAPLOTYPE_X(FunctionInput.ONE_HAPLOTYPE),
    HAS_HOMOZYGOUS_DPYD_DEFICIENCY(FunctionInput.NONE),
    HAS_HETEROZYGOUS_DPYD_DEFICIENCY(FunctionInput.NONE),
    HAS_KNOWN_HPV_STATUS(FunctionInput.NONE),
    OVEREXPRESSION_OF_GENE_X(FunctionInput.ONE_GENE),
    NON_EXPRESSION_OF_GENE_X(FunctionInput.ONE_GENE),
    SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X(FunctionInput.MANY_GENES),
    EXPRESSION_OF_PROTEIN_X_BY_IHC(FunctionInput.ONE_STRING),
    EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y(FunctionInput.ONE_STRING_ONE_INTEGER),
    EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y(FunctionInput.ONE_STRING_ONE_INTEGER),
    EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y(FunctionInput.ONE_STRING_ONE_INTEGER),
    PROTEIN_X_IS_WILD_TYPE_BY_IHC(FunctionInput.ONE_STRING),
    PD_L1_SCORE_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    PD_L1_SCORE_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),
    PD_L1_SCORE_CPS_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    PD_L1_SCORE_CPS_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),
    PD_L1_SCORE_TPS_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_SCORE_TAP_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_SCORE_TPS_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_SCORE_IC_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_SCORE_TC_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_SCORE_TAP_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    PD_L1_STATUS_MUST_BE_AVAILABLE(FunctionInput.NONE),
    HAS_PSMA_POSITIVE_PET_SCAN(FunctionInput.NONE),
    MOLECULAR_RESULTS_MUST_BE_AVAILABLE(FunctionInput.NONE),
    MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X(FunctionInput.ONE_GENE),
    MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X(FunctionInput.ONE_GENE),
    MMR_STATUS_IS_AVAILABLE(FunctionInput.NONE),
    HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES(FunctionInput.NONE),
    HAS_EGFR_PACC_MUTATION(FunctionInput.NONE),
    HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y(FunctionInput.TWO_STRINGS),

    // Rules related to recent laboratory measurements
    HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_INR_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_PT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_PT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_APTT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_APTT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_PTT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_ALBUMIN_LLN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_ASAT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_ALAT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_ASAT_AND_ALAT_ULN_OF_AT_MOST_X_OR_AT_MOST_Y_WHEN_LIVER_METASTASES_PRESENT(FunctionInput.TWO_DOUBLES),
    HAS_ALP_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_ALP_ULN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_INDIRECT_BILIRUBIN_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CREATININE_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_EGFR_CKD_EPI_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_EGFR_MDRD_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_CREATININE_CLEARANCE_BETWEEN_X_AND_Y(FunctionInput.TWO_DOUBLES),
    HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_BNP_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_TROPONIN_IT_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_AMYLASE_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_LIPASE_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_PHOSPHORUS_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_PHOSPHORUS_MMOL_PER_L_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_POTENTIAL_HYPOKALEMIA(FunctionInput.NONE),
    HAS_POTENTIAL_HYPOMAGNESEMIA(FunctionInput.NONE),
    HAS_POTENTIAL_HYPOCALCEMIA(FunctionInput.NONE),
    HAS_POTENTIAL_SYMPTOMATIC_HYPERCALCEMIA(FunctionInput.NONE),
    HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_CORTISOL_LLN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_AFP_ULN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_CA125_ULN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_HCG_ULN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_LDH_ULN_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_PSA_UG_PER_L_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_PSA_LLN_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_URINE_PROTEIN_TO_CREATININE_RATIO_MG_PER_MG_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_GLUCOSE_FASTING_PLASMA_MMOL_PER_L_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_THYROXINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_FREE_TRIIODOTHYRONINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_BOUND_TRIIODOTHYRONINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_TSH_WITHIN_INSTITUTIONAL_NORMAL_LIMITS(FunctionInput.NONE),
    HAS_ANTI_HLA_ANTIBODIES_AGAINST_PDC_LINE(FunctionInput.NONE),

    // Rules related to other conditions
    HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X(FunctionInput.ONE_DOID_TERM),
    HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X_WITHIN_Y_MONTHS(FunctionInput.ONE_DOID_TERM_ONE_INTEGER),
    HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME(FunctionInput.ONE_STRING),
    HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME_WITHIN_Y_MONTHS(FunctionInput.ONE_STRING_ONE_INTEGER),
    HAS_HISTORY_OF_AUTOIMMUNE_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_CARDIAC_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X(FunctionInput.ONE_STRING),
    HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_EYE_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_LIVER_DISEASE(FunctionInput.NONE),
    HAS_HISTORY_OF_LUNG_DISEASE(FunctionInput.NONE),
    HAS_POTENTIAL_RESPIRATORY_COMPROMISE(FunctionInput.NONE),
    HAS_HISTORY_OF_MYOCARDIAL_INFARCT(FunctionInput.NONE),
    HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_HISTORY_OF_PNEUMONITIS(FunctionInput.NONE),
    HAS_HISTORY_OF_STROKE(FunctionInput.NONE),
    HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT(FunctionInput.NONE),
    HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT(FunctionInput.NONE),
    HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT(FunctionInput.NONE),
    HAS_HISTORY_OF_VASCULAR_DISEASE(FunctionInput.NONE),
    HAS_SEVERE_CONCOMITANT_CONDITION(FunctionInput.NONE),
    HAS_HAD_ORGAN_TRANSPLANT(FunctionInput.NONE),
    HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS(FunctionInput.ONE_INTEGER),
    HAS_GILBERT_DISEASE(FunctionInput.NONE),
    HAS_HYPERTENSION(FunctionInput.NONE),
    HAS_HYPOTENSION(FunctionInput.NONE),
    HAS_DIABETES(FunctionInput.NONE),
    HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS(FunctionInput.NONE),
    HAS_POTENTIAL_ABSORPTION_DIFFICULTIES(FunctionInput.NONE),
    HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES(FunctionInput.NONE),
    HAS_POTENTIAL_CONTRAINDICATION_TO_CT(FunctionInput.NONE),
    HAS_POTENTIAL_CONTRAINDICATION_TO_MRI(FunctionInput.NONE),
    HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI(FunctionInput.NONE),
    HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE(FunctionInput.NONE),
    IS_IN_DIALYSIS(FunctionInput.NONE),
    HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE(FunctionInput.ONE_STRING),
    HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY(FunctionInput.NONE),
    HAS_ADEQUATE_VENOUS_ACCESS(FunctionInput.NONE),

    //Rules related to cardiac function
    HAS_POTENTIAL_SIGNIFICANT_HEART_DISEASE(FunctionInput.NONE),
    HAS_ECG_ABERRATION(FunctionInput.NONE),
    HAS_LVEF_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_QTC_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_QTCF_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_QTCF_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_JTC_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_LONG_QT_SYNDROME(FunctionInput.NONE),
    HAS_NORMAL_CARDIAC_FUNCTION_BY_MUGA_OR_TTE(FunctionInput.NONE),
    HAS_FAMILY_HISTORY_OF_IDIOPATHIC_SUDDEN_DEATH(FunctionInput.NONE),
    HAS_FAMILY_HISTORY_OF_LONG_QT_SYNDROME(FunctionInput.NONE),

    // Rules related to infections
    HAS_ACTIVE_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_EBV_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_HEPATITIS_A_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_HEPATITIS_B_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_HEPATITIS_C_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_HIV_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION(FunctionInput.NONE),
    HAS_KNOWN_TUBERCULOSIS_INFECTION(FunctionInput.NONE),
    MEETS_COVID_19_INFECTION_REQUIREMENTS(FunctionInput.NONE),
    HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_RECEIVED_NON_LIVE_VACCINE_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE(FunctionInput.NONE),

    // Rules depending on current medication
    CURRENTLY_GETS_NAME_X_MEDICATION(FunctionInput.ONE_STRING),
    CURRENTLY_GETS_CATEGORY_X_MEDICATION(FunctionInput.ONE_MEDICATION_CATEGORY),
    HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS(FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER),
    CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X(FunctionInput.ONE_CYP),
    CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X(FunctionInput.ONE_CYP),
    HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS(FunctionInput.ONE_CYP_ONE_INTEGER),
    CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ANY_CYP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X(FunctionInput.ONE_CYP),
    CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X(FunctionInput.ONE_CYP),
    CURRENTLY_GETS_MEDICATION_INHIBITING_PGP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_PGP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP(FunctionInput.NONE),
    CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP(FunctionInput.NONE),
    HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING(FunctionInput.NONE),
    CURRENTLY_GETS_HERBAL_MEDICATION(FunctionInput.NONE),

    // Rules related to washout period
    HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS(FunctionInput.MANY_STRINGS_ONE_INTEGER),
    HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES(FunctionInput.MANY_STRINGS_TWO_INTEGERS),
    HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS(FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER),
    HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES(FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS),
    HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS_Y_HALF_LIVES(FunctionInput.TWO_INTEGERS),
    HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS(FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER),
    HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES(FunctionInput.TWO_INTEGERS),
    HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES(FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS),

    // Rules related to pregnancy/anticonception
    IS_BREASTFEEDING(FunctionInput.NONE),
    IS_PREGNANT(FunctionInput.NONE),
    USES_ADEQUATE_ANTICONCEPTION(FunctionInput.NONE),
    ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS(FunctionInput.NONE),

    // Rules related to complications
    HAS_ANY_COMPLICATION(FunctionInput.NONE),
    HAS_COMPLICATION_X(FunctionInput.ONE_STRING),
    HAS_COMPLICATION_OF_CATEGORY_X(FunctionInput.ONE_STRING),
    HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN(FunctionInput.NONE),
    HAS_LEPTOMENINGEAL_DISEASE(FunctionInput.NONE),

    // Rules related to allergies/toxicities
    HAS_INTOLERANCE_TO_NAME_X(FunctionInput.ONE_STRING),
    HAS_INTOLERANCE_BELONGING_TO_DOID_TERM_X(FunctionInput.ONE_DOID_TERM),
    HAS_INTOLERANCE_TO_PLATINUM_COMPOUNDS(FunctionInput.NONE),
    HAS_INTOLERANCE_TO_TAXANE(FunctionInput.NONE),
    HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION(FunctionInput.NONE),
    HAS_INTOLERANCE_FOR_PD_1_OR_PD_L1_INHIBITORS(FunctionInput.NONE),
    HAS_HISTORY_OF_ANAPHYLAXIS(FunctionInput.NONE),
    HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS(FunctionInput.NONE),
    HAS_TOXICITY_OF_AT_LEAST_GRADE_X(FunctionInput.ONE_INTEGER),
    HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y(FunctionInput.ONE_INTEGER_ONE_STRING),
    HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y(FunctionInput.ONE_INTEGER_MANY_STRINGS),

    // Rules related to vital function measurements
    HAS_SBP_MMHG_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    HAS_SBP_MMHG_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),
    HAS_DBP_MMHG_OF_AT_LEAST_X(FunctionInput.ONE_INTEGER),
    HAS_DBP_MMHG_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),
    HAS_PULSE_OXIMETRY_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y(FunctionInput.TWO_DOUBLES),
    HAS_BODY_WEIGHT_OF_AT_LEAST_X(FunctionInput.ONE_DOUBLE),
    HAS_BODY_WEIGHT_OF_AT_MOST_X(FunctionInput.ONE_DOUBLE),
    HAS_BMI_OF_AT_MOST_X(FunctionInput.ONE_INTEGER),

    // Rules related to blood transfusions
    REQUIRES_REGULAR_HEMATOPOIETIC_SUPPORT(FunctionInput.NONE),
    HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS(FunctionInput.ONE_INTEGER),

    // Rules related to surgery
    HAS_HAD_RECENT_SURGERY(FunctionInput.NONE),
    HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS(FunctionInput.ONE_INTEGER),
    HAS_HAD_SURGERY_WITHIN_LAST_X_MONTHS(FunctionInput.ONE_INTEGER),
    HAS_PLANNED_SURGERY(FunctionInput.NONE),
    HAS_HAD_CYTOREDUCTIVE_SURGERY(FunctionInput.NONE)
}
