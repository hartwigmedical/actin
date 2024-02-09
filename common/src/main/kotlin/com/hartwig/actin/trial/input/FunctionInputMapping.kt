package com.hartwig.actin.trial.input

import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.input.single.FunctionInput

object FunctionInputMapping {

    val RULE_INPUT_MAP = mapOf(
        EligibilityRule.IS_AT_LEAST_X_YEARS_OLD to FunctionInput.ONE_INTEGER,
        EligibilityRule.IS_MALE to FunctionInput.NONE,
        EligibilityRule.IS_FEMALE to FunctionInput.NONE,
        EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_WHO_STATUS_OF_AT_EXACTLY_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_LANSKY_SCORE_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT to FunctionInput.NONE,
        EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS to FunctionInput.ONE_INTEGER,
        EligibilityRule.WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X to FunctionInput.ONE_STRING,
        EligibilityRule.IS_LEGALLY_INSTITUTIONALIZED to FunctionInput.NONE,
        EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES to FunctionInput.NONE,

        EligibilityRule.HAS_SOLID_PRIMARY_TUMOR to FunctionInput.NONE,
        EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA to FunctionInput.NONE,
        EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X to FunctionInput.ONE_DOID_TERM,
        EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X_DISTAL_SUB_LOCATION to FunctionInput.ONE_DOID_TERM,
        EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X to FunctionInput.ONE_TUMOR_TYPE,
        EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT to FunctionInput.NONE,
        EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT to FunctionInput.NONE,
        EligibilityRule.HAS_NON_SQUAMOUS_NSCLC to FunctionInput.NONE,
        EligibilityRule.HAS_BREAST_CANCER_RECEPTOR_X_POSITIVE to FunctionInput.ONE_RECEPTOR_TYPE,
        EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_COMPONENT to FunctionInput.NONE,
        EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT to FunctionInput.NONE,
        EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR to FunctionInput.NONE,
        EligibilityRule.HAS_STOMACH_UNDIFFERENTIATED_TUMOR to FunctionInput.NONE,
        EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA to FunctionInput.NONE,
        EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to FunctionInput.NONE,
        EligibilityRule.HAS_STAGE_X to FunctionInput.ONE_TUMOR_STAGE,
        EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_METASTATIC_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_UNRESECTABLE_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_RECURRENT_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_INCURABLE_CANCER to FunctionInput.NONE,
        EligibilityRule.HAS_ANY_LESION to FunctionInput.NONE,
        EligibilityRule.HAS_LIVER_METASTASES to FunctionInput.NONE,
        EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_LIVER_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_CNS_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_BRAIN_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES to FunctionInput.NONE,
        EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_BRAIN_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_EXTRACRANIAL_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_BONE_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_BONE_METASTASES_ONLY to FunctionInput.NONE,
        EligibilityRule.HAS_LUNG_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_LYMPH_NODE_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_VISCERAL_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_UNRESECTABLE_PERITONEAL_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_EXTENSIVE_SYSTEMIC_METASTASES_PREDOMINANTLY_DETERMINING_PROGNOSIS to FunctionInput.NONE,
        EligibilityRule.HAS_BIOPSY_AMENABLE_LESION to FunctionInput.NONE,
        EligibilityRule.HAS_IRRADIATION_AMENABLE_LESION to FunctionInput.NONE,
        EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES to FunctionInput.ONE_INTEGER,
        EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to FunctionInput.NONE,
        EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to FunctionInput.NONE,
        EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY to FunctionInput.NONE,
        EligibilityRule.HAS_EVALUABLE_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_MEASURABLE_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST to FunctionInput.NONE,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA to FunctionInput.NONE,
        EligibilityRule.HAS_INJECTION_AMENABLE_LESION to FunctionInput.NONE,
        EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION to FunctionInput.NONE,
        EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI to FunctionInput.NONE,
        EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI to FunctionInput.NONE,
        EligibilityRule.HAS_SUPERSCAN_BONE_SCAN to FunctionInput.NONE,
        EligibilityRule.HAS_BCLC_STAGE_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT to FunctionInput.NONE,
        EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR to FunctionInput.NONE,

        EligibilityRule.IS_NOT_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT to FunctionInput.NONE,
        EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X to FunctionInput.ONE_STRING,
        EligibilityRule.IS_ELIGIBLE_FOR_PALLIATIVE_RADIOTHERAPY to FunctionInput.NONE,
        EligibilityRule.IS_ELIGIBLE_FOR_LOCO_REGIONAL_THERAPY to FunctionInput.NONE,
        EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X to FunctionInput.MANY_INTEGERS,
        EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE,
        EligibilityRule.HAS_HAD_TREATMENT_NAME_X to FunctionInput.ONE_SPECIFIC_TREATMENT,
        EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER,
        EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X to FunctionInput.MANY_DRUGS,
        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS to FunctionInput.MANY_STRINGS_ONE_INTEGER,
        EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to
                FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES,
        EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y to FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y to FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE,
        EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_MONTHS to FunctionInput.MANY_INTENTS_ONE_INTEGER,
        EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X to FunctionInput.MANY_INTENTS,
        EligibilityRule.HAS_HAD_NON_INTERNAL_RADIOTHERAPY to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES to FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to FunctionInput.ONE_SPECIFIC_TREATMENT,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT to FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES to
                FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER,
        EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_COMPLETE_RESECTION to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_PARTIAL_RESECTION to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to FunctionInput.NONE,
        EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL to FunctionInput.NONE,
        EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL to FunctionInput.NONE,
        EligibilityRule.IS_NOT_PARTICIPATING_IN_ANOTHER_TRIAL to FunctionInput.NONE,
        EligibilityRule.HAS_RECEIVED_POTENTIAL_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_BRAIN_RADIATION_THERAPY to FunctionInput.NONE,

        EligibilityRule.HAS_ACTIVE_SECOND_MALIGNANCY to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_TERM_X to FunctionInput.ONE_DOID_TERM,
        EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS to FunctionInput.ONE_INTEGER,

        EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to FunctionInput.MANY_GENES,
        EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.INACTIVATION_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y to FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS,
        EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y to FunctionInput.ONE_GENE_MANY_CODONS,
        EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y to FunctionInput.ONE_GENE_ONE_INTEGER,
        EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z to FunctionInput.ONE_GENE_TWO_INTEGERS,
        EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z to FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE,
        EligibilityRule.UTR_3_LOSS_IN_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.AMPLIFICATION_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES to FunctionInput.ONE_GENE_ONE_INTEGER,
        EligibilityRule.FUSION_IN_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.WILDTYPE_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.EXON_SKIPPING_GENE_X_EXON_Y to FunctionInput.ONE_GENE_ONE_INTEGER,
        EligibilityRule.MSI_SIGNATURE to FunctionInput.NONE,
        EligibilityRule.HRD_SIGNATURE to FunctionInput.NONE,
        EligibilityRule.TMB_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.TML_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.TML_BETWEEN_X_AND_Y to FunctionInput.TWO_INTEGERS,
        EligibilityRule.HAS_HLA_TYPE_X to FunctionInput.ONE_HLA_ALLELE,
        EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to FunctionInput.ONE_HAPLOTYPE,
        EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to FunctionInput.NONE,
        EligibilityRule.OVEREXPRESSION_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.NON_EXPRESSION_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to FunctionInput.ONE_STRING,
        EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to FunctionInput.ONE_STRING_ONE_INTEGER,
        EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to FunctionInput.ONE_STRING_ONE_INTEGER,
        EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to FunctionInput.ONE_STRING_ONE_INTEGER,
        EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to FunctionInput.ONE_STRING,
        EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE to FunctionInput.NONE,
        EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN to FunctionInput.NONE,
        EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE to FunctionInput.NONE,
        EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X to FunctionInput.ONE_GENE,
        EligibilityRule.NSCLC_DRIVER_GENE_STATUSES_MUST_BE_AVAILABLE to FunctionInput.NONE,

        EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_APTT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_PTT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_ALP_ULN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CREATININE_CLEARANCE_BETWEEN_X_AND_Y to FunctionInput.TWO_DOUBLES,
        EligibilityRule.HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TROPONIN_IT_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_AMYLASE_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LIPASE_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_SYMPTOMATIC_HYPERCALCEMIA to FunctionInput.NONE,
        EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_HYPOMAGNESEMIA to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_HYPOCALCEMIA to FunctionInput.NONE,
        EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PSA_UG_PER_L_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PSA_LLN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_PHOSPHORUS_MMOL_PER_L_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_URINE_PROTEIN_TO_CREATININE_RATIO_MG_PER_MG_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_GLUCOSE_FASTING_PLASMA_MMOL_PER_L_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_THYROXINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to FunctionInput.NONE,

        EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X to FunctionInput.ONE_DOID_TERM,
        EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_STROKE to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_GILBERT_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_HYPERTENSION to FunctionInput.NONE,
        EligibilityRule.HAS_HYPOTENSION to FunctionInput.NONE,
        EligibilityRule.HAS_DIABETES to FunctionInput.NONE,
        EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI to FunctionInput.NONE,
        EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI to FunctionInput.NONE,
        EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE to FunctionInput.NONE,
        EligibilityRule.IS_IN_DIALYSIS to FunctionInput.NONE,
        EligibilityRule.HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY to FunctionInput.NONE,

        EligibilityRule.HAS_POTENTIAL_SIGNIFICANT_HEART_DISEASE to FunctionInput.NONE,
        EligibilityRule.HAS_ECG_ABERRATION to FunctionInput.NONE,
        EligibilityRule.HAS_LVEF_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_QTC_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_QTCF_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_QTCF_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_JTC_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_LONG_QT_SYNDROME to FunctionInput.NONE,
        EligibilityRule.HAS_NORMAL_CARDIAC_FUNCTION_BY_MUGA_OR_TTE to FunctionInput.NONE,
        EligibilityRule.HAS_FAMILY_HISTORY_OF_IDIOPATHIC_SUDDEN_DEATH to FunctionInput.NONE,
        EligibilityRule.HAS_FAMILY_HISTORY_OF_LONG_QT_SYNDROME to FunctionInput.NONE,

        EligibilityRule.HAS_ACTIVE_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_EBV_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_HIV_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION to FunctionInput.NONE,
        EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION to FunctionInput.NONE,
        EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS to FunctionInput.NONE,
        EligibilityRule.HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS to FunctionInput.ONE_INTEGER,
        EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE to FunctionInput.NONE,

        EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION to FunctionInput.ONE_STRING,
        EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS to FunctionInput.ONE_STRING_ONE_INTEGER,
        EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X to FunctionInput.ONE_STRING,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS to FunctionInput.ONE_STRING_ONE_INTEGER,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ANY_CYP to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X to FunctionInput.ONE_STRING,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X to FunctionInput.ONE_STRING,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_PGP to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP to FunctionInput.NONE,
        EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING to FunctionInput.NONE,
        EligibilityRule.CURRENTLY_GETS_HERBAL_MEDICATION to FunctionInput.NONE,

        EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS to FunctionInput.MANY_STRINGS_ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to FunctionInput.MANY_STRINGS_TWO_INTEGERS,
        EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS to FunctionInput.MANY_STRINGS_ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to FunctionInput.MANY_STRINGS_TWO_INTEGERS,
        EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS_Y_HALF_LIVES to FunctionInput.TWO_INTEGERS,
        EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS to FunctionInput.MANY_STRINGS_ONE_INTEGER,
        EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES to FunctionInput.TWO_INTEGERS,
        EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES to
                FunctionInput.MANY_STRINGS_TWO_INTEGERS,

        EligibilityRule.IS_BREASTFEEDING to FunctionInput.NONE,
        EligibilityRule.IS_PREGNANT to FunctionInput.NONE,
        EligibilityRule.USES_ADEQUATE_ANTICONCEPTION to FunctionInput.NONE,
        EligibilityRule.ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS to FunctionInput.NONE,

        EligibilityRule.HAS_ANY_COMPLICATION to FunctionInput.NONE,
        EligibilityRule.HAS_COMPLICATION_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_COMPLICATION_OF_CATEGORY_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN to FunctionInput.NONE,
        EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE to FunctionInput.NONE,

        EligibilityRule.HAS_INTOLERANCE_TO_NAME_X to FunctionInput.ONE_STRING,
        EligibilityRule.HAS_INTOLERANCE_BELONGING_TO_DOID_TERM_X to FunctionInput.ONE_DOID_TERM,
        EligibilityRule.HAS_INTOLERANCE_TO_PLATINUM_COMPOUNDS to FunctionInput.NONE,
        EligibilityRule.HAS_INTOLERANCE_TO_TAXANE to FunctionInput.NONE,
        EligibilityRule.HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION to FunctionInput.NONE,
        EligibilityRule.HAS_INTOLERANCE_FOR_PD_1_OR_PD_L1_INHIBITORS to FunctionInput.NONE,
        EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS to FunctionInput.NONE,
        EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS to FunctionInput.NONE,
        EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y to FunctionInput.ONE_INTEGER_ONE_STRING,
        EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y to FunctionInput.ONE_INTEGER_MANY_STRINGS,

        EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_SBP_MMHG_OF_AT_MOST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_DBP_MMHG_OF_AT_MOST_X to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_PULSE_OXIMETRY_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_BODY_WEIGHT_OF_AT_LEAST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_BODY_WEIGHT_OF_AT_MOST_X to FunctionInput.ONE_DOUBLE,
        EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y to FunctionInput.TWO_DOUBLES,
        EligibilityRule.HAS_BMI_OF_AT_MOST_X to FunctionInput.ONE_INTEGER,

        EligibilityRule.REQUIRES_REGULAR_HEMATOPOIETIC_SUPPORT to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS to FunctionInput.ONE_INTEGER,

        EligibilityRule.HAS_HAD_RECENT_SURGERY to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_MONTHS to FunctionInput.ONE_INTEGER,
        EligibilityRule.HAS_PLANNED_SURGERY to FunctionInput.NONE,
        EligibilityRule.HAS_HAD_CYTOREDUCTIVE_SURGERY to FunctionInput.NONE
    )
}
