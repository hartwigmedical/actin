## ACTIN-Algo

ACTIN-Algo matches a patient record (clinical & molecular data) with available treatments.

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.algo.TreatmentMatcherApplication \
   -clinical_json /path/to/clinical.json \
   -molecular_json /path/to/molecular.json \
   -treatment_database_directory /path/to/potential_treatment_options \
   -doid_json /path/to/full_doid_tree_json_file \
   -output_directory /path/where/output/is/written \
```

The following assumptions are made about the inputs:
 - The clinical JSON adheres to the datamodel defined by [ACTIN-Clinical](../clinical/README.md)
 - The molecular JSON adheres to the datamodel defined by [ACTIN-Molecular](../molecular/README.md) 
 - The treatment database directory is the output directory of [ACTIN-Treatment](../treatment/README.md)
 
### Treatment matching

Every treatment defined in the treatment database is evaluated independently. 

In case a treatment is a trial, all relevant inclusion and exclusion criteria are evaluated for this trial as well as every criterion 
for any specific cohort within this trial.  

Every criterion evaluates to one of the following options:

Evaluation | Description
---|---
PASS | The patient complies with the inclusion or exclusion criterion.  
PASS_BUT_WARN | The patient complies with the inclusion or exclusion criterion but a manual check is required.
FAIL | The patient does not comply with the inclusion or exclusion criterion. 
UNDETERMINED | The data provided to the inclusion or exclusion criterion is insufficient for determining eligibility.
NOT_EVALUATED | The evaluation of the inclusion or exclusion criterion is skipped and can be assumed to be irrelevant for determining trial eligibility. 
NOT_IMPLEMENTED | No algo has been implemented yet for this criterion.

For every trial (and cohort) an overall evaluation is determined using the following algorithm:
 1. A patient is eligible for a trial (or cohort) only in case no criteria are `PASS`, `PASS_BUT_WARN` or `NOT_EVALUATED`
 1. In case one of the criteria evaluates to a `FAIL` or `NOT_IMPLEMENTED` the patient fails overall eligibility for the trial (or cohort). 
 1. In case of no fails but at least one `UNDETERMINED` evaluation, the overall evaluation is determined to be `UNDETERMINED`. 
 Trials (and cohorts) with this evaluation are considered _potentially_ eligible.    

The following additional rules are applied for trials versus cohorts:
 1. A patient is eligible for a specific cohort only if the overall evaluation for both the cohort as well as the trial itself are passed.
 1. A patient is eligible for a trial only if it is eligible for at least one of the cohorts within the trial or if the trial has no 
 cohorts defined and the patient passes all criteria for the trial.
   
#### Individual criteria algorithms

Inclusion and exclusion criteria can be defined as a set of rules that are combined using composite functions, to determine overall eligibility. 

The following composite functions are available:

Function | Description 
---|---
AND | indicates that all combined rules should PASS in order to PASS
OR | indicates that one of the combined rules should PASS in order to PASS
NOT | indicates that the rule should not be PASS in order to PASS
WARN_ON_PASS | indicates that a warning should be displayed in case of PASS and resolves to PASS_BUT_WARN

Some rules require 1 ("X") or more ("X" and "Y") additional configuration parameter(s) that can be set to match the requirements of each trial. 
Also, note that some inclusion and exclusion criteria can be mapped to rules that are currently explicitly set to PASS or explicitly 
won't be evaluated. 

The following rules are available:

##### Rules related to general patient characteristics

Rule | When does a patient pass evaluation? | Note
---|---|---
IS_AT_LEAST_X_YEARS_OLD | Current year minus birth year > X | `UNDETERMINED` in case of exactly X
IS_MALE | Patient > Gender = Male
HAS_WHO_STATUS_OF_AT_MOST_X | WHO <= X
CAN_GIVE_ADEQUATE_INFORMED_CONSENT | > won't be evaluated
IS_INVOLVED_IN_STUDY_PROCEDURES | > won't be evaluated
IS_PARTICIPATING_IN_ANOTHER_TRIAL | > won't be evaluated
HAS_PARTICIPATED_IN_CURRENT_TRIAL | T.B.D. | Currently always set to `UNDETERMINED`
HAS_RAPIDLY_DETERIORATING_CONDITION | > won't be evaluated
HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS | > won't be evaluated
HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS | > won't be evaluated
PATIENT_IS_TREATED_IN_HOSPITAL_X | > won't be evaluated
PATIENT_WILL_BE_PARTICIPATING_IN_COUNTRY_X | > currently set to Netherlands (T.B.D.)
PATIENT_IS_LEGALLY_INSTITUTIONALIZED | > won't be evaluated
IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES | > will resolve to PASS_BUT_WARN

##### Rules related to tumor and lesion locations
 
Rule | When does a patient pass evaluation?
---|---
PRIMARY_TUMOR_LOCATION_BELONGS_ TO_DOID_X | Configured DOID should be equal or be a child of DOID X
HAS_MELANOMA_OF_UNKNOWN_PRIMARY | All configured DOIDs are equal to 1909 
HAS_STAGE_X | Tumor details > stage. X one of: I, II, III, IIIA, IIIB, IIIC, IV
HAS_ADVANCED_CANCER | Tumor details > stage III or IV
HAS_METASTATIC_CANCER | Tumor details > stage IV 
HAS_ANY_LESION | Tumor details > Either hasLiverLesion, hasCnsLesions, hasBrainLesions, hasBoneLesions or hasOtherLesions = 1
HAS_LIVER_METASTASES | Tumor details > hasLiverLesions = 1
HAS_KNOWN_CNS_METASTASES | Tumor details > hasCnsLesions = 1
HAS_KNOWN_ACTIVE_CNS_METASTASES | Tumor details > hasActiveCnsLesions = 1
HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES | Tumor details > hasSymptomaticCnsLesions = 1
HAS_KNOWN_BRAIN_METASTASES | Tumor details > hasBrainLesions = 1
HAS_KNOWN_ACTIVE_BRAIN_METASTASES | Tumor details > hasActiveBrainLesions = 1
HAS_KNOWN_SYMPTOMATIC_BRAIN_METASTASES | Tumor details > hasSymptomaticBrainLesions = 1
HAS_BONE_METASTASES | Tumor details > hasBoneLesions = 1
HAS_LUNG_METASTASES | Tumor details > otherLesionDescription like %Pulmonal% or %Lung%
HAS_MEASURABLE_DISEASE_RECIST | Tumor details > hasMeasurableDiseaseRecist = 1 
HAS_BIOPSY_AMENABLE_LESION | Presence of WGS (to be further extended)
HAS_INJECTION_AMENABLE_LESION | Currently resolves to undetermined
HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA | Currently resolves to undetermined
HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION | Currently resolves to undetermined
HAS_SUPERSCAN_BONE_SCAN | won't be evaluated
HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT | Currently resolves to undetermined
HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_ X_MONTHS_BEFORE_IC | Presence of WGS (to be extended)
HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE | won't be evaluated

##### Rules related to previous cancer treatments

Rule | When does a patient pass evaluation? | Note
---|---|---
IS_ELIGIBLE_FOR_TREATMENT_WITH_ CURATIVE_INTENT | T.B.D. - currently not evaluated
HAS_EXHAUSTED_SOC_TREATMENTS | T.B.D. - currently resolves to PASS_BUT_WARN
HAS_DECLINED_SOC_TREATMENTS | T.B.D. - currently not evaluated
HAS_HAD_AT_LEAST_X_ APPROVED_TREATMENT_LINES | T.B.D. - current undetermined
HAS_HAD_AT_LEAST_X_SYSTEMIC_ TREATMENT_LINES | Prior tumor treatments > nr of lines in case systemic = 1 => X
HAS_HAD_AT_MOST_X_SYSTEMIC_ TREATMENT_LINES | Prior tumor treatments > nr of lines in case systemic = 1 <= X
HAS_HAD_TREATMENT_NAME_X | Prior tumor treatments > name contains X
HAS_HAD_CATEGORY_X_TREATMENT | Prior tumor treatments > categories contains X | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Radiotherapy, Surgery, Transplantation, Antiviral therapy, Vaccine, Car T
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y | Prior tumor treatments > categories contains "X" and corresponding type like %Y% | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Transplantation, Car T (since these have a corresponding type in model)
HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPE_Y | Prior tumor treatments > categories contains "X" and corresponding type like %Y% | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Transplantation, Car T (since these have a corresponding type in model). Multiple names can be specified within 1 rule, separated by ";"
HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES | Prior tumor treatments > categories contains "X" and number of lines => Y | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Radiotherapy, Surgery, Transplantation, Antiviral therapy, Vaccine, Car T
HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES | Prior tumor treatments > categories contains "X" and number of lines <= Y | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Radiotherapy, Surgery, Transplantation, Antiviral therapy, Vaccine, Car T
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_ AND_AT_LEAST_Z_LINES | categories contains "X" and corresponding type like %Y% and number of lines => Z | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Transplantation, Car T (since these have a corresponding type in model)
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_ AND_AT_MOST_Z_LINES | categories contains "X" and corresponding type like %Y% and number of lines <= Z | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Transplantation, Car T (since these have a corresponding type in model)
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_ AND_AT_MOST_Z_LINES | categories contains "X" and corresponding types like %Y% (split per ";") and distinct number of lines <= Z | "X" can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Transplantation, Car T (since these have a corresponding type in model). 
HAS_HAD_FLUOROPYRIMIDINE_TREATMENT | Prior tumor treatments > name contains any fluoropyrimidine | Fluoropyrimidines: Capecitabine, Carmofur, Doxifluridine, Fluorouracil, Tegafur (T.B.D.)
HAS_HAD_TAXANE_TREATMENT | Prior tumor treatments > name contains any taxane | Taxanes: Paclitaxel, Docetaxel, Cabazitaxel (T.B.D.)
HAS_HAD_TAXANE_TREATMENT_AND_AT_MOST_X_LINES | Prior tumor treatments > name contains any taxane and and number of lines <= X  | Taxanes: Paclitaxel, Docetaxel, Cabazitaxel (T.B.D.)
HAS_HAD_TYROSINE_KINASE_TREATMENT |  Prior tumor treatments > category = 'Targeted therapy' & T.B.D.
HAS_HAD_INTRATUMURAL_INJECTION_TREATMENT | T.B.D
IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X | Drug X is in the SOC treatment DB for that tumor type (T.B.I.)

##### Rules related to prior primary tumors

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_HISTORY_OF_SECOND_MALIGNANCY | Prior second primaries is not empty
HAS_HISTORY_OF_SECOND_MALIGNANCY_ BELONGING_TO_DOID_X | Presence of prior second primary belonging to DOID X
HAS_HISTORY_OF_SECOND_MALIGNANCY_ BELONGING_TO_DOID_X_CURRENTLY_INACTIVE | Presence of prior second primary belonging to DOID X, and status is inactive
EVERY_SECOND_MALIGNANCY_HAS_BEEN_ CURED_SINCE_X_YEARS | Prior second primaries is empty OR every prior second primary is inactive | Years can often not be reliably evaluated; rule will be combined with WARN_ON_PASS

##### Rules related to molecular results

Rule | When does a patient pass evaluation?
---|---
MOLECULAR_RESULTS_MUST_BE_AVAILABLE | Ingestion of ORANGE results (later to be extended)
MOLECULAR_RESULTS_MUST_BE_AVAILABLE_ FOR_GENE_X | Ingestion of ORANGE results (later to be extended)
ACTIVATION_OR_AMPLIFICATION_OF_GENE_X | Activating mutation or amplification is found in gene X
INACTIVATION_OF_GENE_X | Inactivating mutation or deletion/disruption is found in gene X
ACTIVATING_MUTATION_IN_GENE_X | Activating mutation is found in gene X
MUTATION_IN_GENE_X_OF_TYPE_Y | Specific mutation Y is found in gene X
AMPLIFICATION_OF_GENE_X | Amplification is found in gene X
DELETION_OF_GENE_X | Deletion is found in gene X
FUSION_IN_GENE_X | Driver fusion with fusion partner gene X is found 
SPECIFIC_FUSION_OF_X_TO_Y | Driver fusion with 2 specified fusion partner genes is found
OVEREXPRESSION_OF_GENE_X | > Currently set to fail (T.B.D.)
NON_EXPRESSION_OF_GENE_X | > Currently set to fail (T.B.D.)
EXPRESSION_OF_GENE_X_BY_IHC | Prior molecular test > Test = IHC, Item = X and (scoreText = positive or scoreValue>0)
EXPRESSION_OF_GENE_X_BY_IHC_OF_EXACTLY_Y | Prior molecular test > Test = IHC, Item = X and scoreValue = Y
EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y | Prior molecular test > Test = IHC, Item = X and scoreValue => Y
WILDTYPE_OF_GENE_X | No driver mutation is found in gene X
MSI_SIGNATURE | MS Status = MSI
HRD_SIGNATURE | HR Status = HRD
TMB_OF_AT_LEAST_X | Tumor Mutational Burden (TMB) should be => X
TML_OF_AT_LEAST_X | Tumor Mutational Load (TML) should be => X
TML_OF_AT_MOST_X | TML should be <= X
PD_L1_SCORE_CPS_OF_AT_LEAST_X | Prior molecular test > Test = IHC, Item = PD-L1, measure = CPS, scoreValue => X
PD_L1_SCORE_CPS_OF_AT_MOST_X | Prior molecular test > Test = IHC, Item = PD-L1, measure = CPS, scoreValue <= X

##### Rules related to recent laboratory measurements

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X | Leukocytes absolute (LEUKO-ABS) in 10^9/L => X
HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X | Leukocytes absolute (LEUKO-ABS) in 10^9/L => X*LLN
HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X | Neutrophil granulocytes absolute (NEUTRO-ABS/NEUTRO-ABS-eDA) in 10^9/L or 10*9/L => X 
HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X | Thrombocytes absolute (THROMBO-ABS) in 10*9/L => X 
HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X | Lymphocytes absolute (LYMPHO-ABS-eDA/LYMPHO-ABS-eDM) in 10*9/L => X
HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X | Albumin (ALB) in g/dL => X. | In case ALB is measured in g/L, the value is converted using ALB[g/dL]=ALB[g/L]/10.
HAS_ALBUMIN_LLN_OF_AT_LEAST_X | Albumin (ALB) > X*ULN
HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X | Hemoglobin (Hb) in g/dL => X. | In case Hb is measured in mmol/L, the value is converted to g/dL using Hb[g/dL]=Hb[mmol/L]/0.6206
HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X | Hemoglobin (Hb) in mmol/L => X. | In case Hb is measured in g/dL, the value is converted to mmol/L using Hb[mmol/L]=Hb[g/dL]*0.6206
HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X | Glucose /PL (GL_P) in mmol/L <= X
HAS_SERUM_TESTOSTERONE_NG_PER_DL_ OF_AT_MOST_X | Serum testosterone (T.B.D.) in ng/dL <= X
HAS_EGFR_CKD_EPI_OF_AT_LEAST_X | eGFR (CKD-EPI formula) => X. | In case CrCl is measured in another unit, the value is converted using
HAS_EGFR_MDRD_OF_AT_LEAST_X | eGFR (MDRD formula) => X. | In case CrCl is measured in another unit, the value is converted using
HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X | Creatinine clearance (Cockcroft Gault formula) => X. | In case CrCl is measured in another unit, the value is converted using
HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X | Creatinine (CREA) in mg/dL <= X | In case CREA is measured in umol/l, the value is converted using CREA[mg/dL]=CREA[umol/l]/88.42
HAS_CREATININE_ULN_OF_AT_MOST_X | Creatinine (CREA) <= X*ULN 
HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X | Total Bilirubin (TBIL) <= X*ULN
HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X | Total Bilirubin (TBIL) in umol/L <= X
HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X | Direct Bilirubin (DBIL) <= X*ULN 
HAS_INR_ULN_OF_AT_MOST_X | International normalized ratio (INR/POCT_INR) <= X*ULN 
HAS_PT_ULN_OF_AT_MOST_X | Prothrombin time (PT/POCT_PT) <= X*ULN 
HAS_APTT_ULN_OF_AT_MOST_X | Activated partial thromboplastin time (APTT) <= X*ULN 
HAS_PTT_ULN_OF_AT_MOST_X | T.B.D. 
HAS_ASAT_ULN_OF_AT_MOST_X | Aspartate aminotransferase (ASAT) <= X*ULN 
HAS_ALAT_ULN_OF_AT_MOST_X | Alanine aminotransferase (ALAT) <= X*ULN
HAS_ALP_ULN_OF_AT_MOST_X | Alkaline phosphatase (ALP) <= X*ULN
HAS_LDH_ULN_OF_AT_MOST_X | Lactate dehydrogenase (LDH) <= X*ULN
HAS_PHOSPHORUS_ULN_OF_AT_MOST_X | Phosphate (P) <= X*ULN
HAS_AFP_ULN_OF_AT_LEAST_X | Alpha fetoprotein (AFP) <= X*ULN
HAS_CA125_ULN_OF_AT_LEAST_X | CA 125 (C125) <= X*ULN
HAS_HCG_ULN_OF_AT_LEAST_X | HCG + beta HCG (HCG) <= X*ULN
HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X | Calcium (Ca) in mg/dL <= X | In case calcium is measured in mmol/L, the value is converted to mg/dL using Ca[mg/dL]=Ca[mmol/L]/0.2495
HAS_CALCIUM_MMOL_PER_L_OF_AT_MOST_X | Calcium (Ca) in mmol/L <= X | In case calcium is measured in mg/dL, the value is converted to mmol/L using Ca[mmol/L]=Ca=[mg/dL]*0.2495
HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X | Iononized calcium (B_ICA) in mmol/L <= X
HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X | Calcium corrected (Ca_C) <= X*ULN
HAS_SERUM_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X | Potassium (K) in mmol/l => X
HAS_BNP_ULN_OF_AT_MOST_X | NT-pro-BNP (BNP) <= X*ULN
HAS_TROPONIN_IT_ULN_OF_AT_MOST_X | High-sensitivity Troponin T (HSTNT) <= X*ULN
HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X | Triglyceride (TG) <= X
HAS_POTASSIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Potassium (K) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)
HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Magnesium (MG) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)
HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Phosphate (P) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)
HAS_CALCIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Calcium (Ca) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_CALCIUM_WITHIN_ INSTITUTIONAL_NORMAL_LIMITS | Calcium corrected (Ca_C) LLN<X<ULN (isOutsideRef=0)
HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X | Total protein in urine (TE_U) => X.
HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X | In g/24h. T.B.D

ULN = Upper Limit of Normal, LLN = Lower Limit of Normal; implemented as refLimitUp and refLimitLow, respectively.

Note: for all lab values, the latest available lab value (up to 30 days) is evaluated. 
If the latest lab value is out of the requested range, the second-last lab value is evaluated. 
In case that a second-last lab value is available and within requested range as configured in the rule, the evaluation resolves to UNDETERMINED. In case no second-last value is applicable, 
or that this value is also out of requested range, the evaluation resolves to FAIL.

##### Rules related to other conditions

Rule | When does a patient pass evaluation?
---|---
HAS_HISTORY_OF_AUTOIMMUNE_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 417
HAS_HISTORY_OF_CARDIAC_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 114
HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 1287
HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 77
HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 2914
HAS_HISTORY_OF_VASCULAR_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 178
HAS_HISTORY_OF_LUNG_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 850
HAS_HISTORY_OF_LIVER_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 409
HAS_HISTORY_OF_STROKE | Prior other conditions > any configured doid should be equal or be a child of DOID 6713 
HAS_HISTORY_OF_TIA | Prior other conditions > any configured doid should be equal or be a child of DOID 224 
HAS_HISTORY_OF_SPECIFIC_CONDITION_ WITH_DOID_X | Prior other conditions > any configured doid should be equal or be a child of DOID "X"
HAS_HISTORY_OF_SPECIFIC_CONDITION_ X_BY_NAME | Prior other conditions > name like %X%
HAS_HAD_ORGAN_TRANSPLANT | Prior other conditions > categories contains "Organ transplant"
HAS_GILBERT_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 2739
HAS_HYPERTENSION | Prior other conditions > any configured doid should be equal or be a child of DOID 10763
HAS_DIABETES | Prior other conditions > any configured doid should be equal or be a child of DOID 9351
HAS_HISTORY_OF_ANAPHYLAXIS | Resolves to undetermined in case of presence of any allergies (T.B.D.)
HAS_POTENTIAL_ABSORPTION_DIFFICULTIES | Or: Prior other condition belonging to DOID 77, Complication of name %Diarrhea%, %Nausea%, %Small bowel resection%, %Colectomy%, %Vomit%, Toxicity source questionnaire or EHR grade=>2 of name %Diarrhea%, %Nausea%, %Vomit%
HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES | Or: Has complication of name %tube%, %swallow% (T.B.D.)
HAS_POTENTIAL_CONTRAINDICATION_TO_MRI | > prior other condition > category like %Implant% or name like %Claustrophobia% or any other condition belonging to DOID 557, or allergy name like %contrast agent%
IS_IN_DIALYSIS | won't be evaluated
HAS_ADEQUATE_VEIN_ACCESS_FOR_LEUKAPHERESIS | currently resolves to undetermined
HAS_SEVERE_CONCOMITANT_CONDITION | won't be evaluated

##### Rules related to cardiac function

Rule | When does a patient pass evaluation?
---|---
HAS_CARDIAC_ARRHYTHMIA | Clinical status > hasSigAberrationLatestECG = 1
HAS_CARDIAC_ARRHYTHMIA_OF_TYPE_X | Clinical status > sigAberrationLatestECG like %X%
HAS_LVEF_OF_AT_LEAST_X | clinicalStatus > lvef should be => x. Unavailable LVEF data leads to UNDETERMINED, out of range LVEF leads to FAIL
HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN | clinicalStatus > lvef should be => X. Unavailable LVEF data leads to PASS, out of range LVEF leads to FAIL
HAS_QTCF_OF_AT_MOST_X | Clinical status > qtcfValue in ms <= X
HAS_LONG_QT_SYNDROME | Prior other conditions > any configured doid should be equal or be a child of DOID 2843
HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y | T.B.D.

##### Rules related to infections

Rule | When does a patient pass evaluation?
---|---
HAS_ACTIVE_INFECTION | Clinical status > hasActiveInfection = 1
HAS_KNOWN_HEPATITIS_A_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 12549
HAS_KNOWN_HEPATITIS_B_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 2043
HAS_KNOWN_HEPATITIS_C_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 1883
HAS_KNOWN_HIV_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 526
HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION |  Prior other conditions > configured doid should be equal or be a child of DOID 0080827
HAS_KNOWN_TUBERCOLOSIS_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 399
HAS_CURRENT_COVID_19_INFECTION | T.B.D.
ADHERENCE_TO_PROTOCOL_REGARDING_ ATTENUATED_VACCINE_USE | > won't be evaluated. 

##### Rules related to allergies / current medication 

Rule | When does a patient pass evaluation?| Note
---|---|---
HAS_ALLERGY_OF_NAME_X | Allergy > Name like %X%
HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION | Allergy > Category = medication AND clinicalStatus = active | Resolves to Undetermined, since exact ingredients cannot yet be automatically evaluated
CURRENTLY_GETS_MEDICATION | Medication > Any medication exists with status active
CURRENTLY_GETS_NAME_X_MEDICATION | Medication > name like %X%
CURRENTLY_GETS_CATEGORY_X_MEDICATION | Medication > categories like "X"
CURRENTLY_GETS_ANTICOAGULANT_MEDICATION | Medication > categories contains type of "Anticoagulants" or "Vitamin K antagonists" and status is active
CURRENTLY_GETS_AZOLE_MEDICATION | Medication > categories contains type of "Triazoles" or "Imidazoles, cutaneous" or "Imidazoles, other" and status is active
CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION | Medication > categories contains type of "Bisphosphonates" or "Calcium regulatory medication" and status is active
CURRENTLY_GETS_CORTICOSTEROID_MEDICATION | Medication > categories contains type of "Corticosteroids" and status is active
CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION | Medication > categories contains type of "Vitamin K Antagonists" and status is active
CURRENTLY_GETS_GONADORELIN_MEDICATION | Medication > categories contains type of "Gonadorelin antagonists" or "Gonadorelin agonists"
CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION | T.B.D. - categories contains type of "Immunosuppressants, selective" or "Immunosuppresants, other"
CURRENTLY_GETS_OAT3_INHIBITORS_MEDICATION | T.B.D. - name like Probenecid, Rifampicin, Novobiocin, Cabotegravir
CURRENTLY_GETS_PAIN_MEDICATION | Medication > categories contains type of "NSAIDs", "Opioids", or name like %Paracetamol% or %Amitriptyline% or %Pregabalin% (T.B.E.)
CURRENTLY_GETS_PROHIBITED_MEDICATION | T.B.D. - Currently resolves to Undetermined
CURRENTLY_GETS_POTENTIALLY_QT_ PROLONGATING_MEDICATION | T.B.D. - Currently resolves to UNDETERMINED
CURRENTLY_GETS_COLONY_STIMULATING_FACTORS | Medication > categories contains type of "Colony stimulating factors" and status is active
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_CYP_X | T.B.D. - Currently resolves to UNDETERMINED | Cytochrome P450 enzymes
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_PGP | T.B.D. - Currently resolves to UNDETERMINED | P-glycoprotein
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_OATP_X | T.B.D. - Currently resolves to UNDETERMINED | Organic-anion-transporting polypeptides
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_BCRP | T.B.D. - Currently resolves to UNDETERMINED | 
HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING | Medication > categories contains "Anticoagulants" AND only 1 distinct dosage
HAS_STABLE_PAIN_MEDICATION_DOSING | Medication > categories contains type of "NSAIDs", "Opioids", or name like %Paracetamol% or %Amitriptyline% or %Pregabalin% AND only 1 distinct dosage per name (T.B.E.)

##### Rules related to washout period 

Rule | When does a patient pass evaluation?| Note
---|---|---
HAS_RECEIVED_DRUG_X_CANCER_THERAPY_ WITHIN_Y_WEEKS | medication > name like %X% | 
HAS_RECEIVED_CATEGORY_X_CANCER_THERAPY_ WITHIN_Y_WEEKS | medication > categories like %X% OR if category name is present in category list **, use category config | 
HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS | Radiotherapy in treatment history when: 1] no date provided; 2] in case only a year is provided then in case of current year; 3] in case year+month is provided then in case of current year and current month | 
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ WITHIN_X_WEEKS | Any medication corresponding to categories in anti-cancer medication list* within X weeks compared to current date (check note) | Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ EXCL_CATEGORY_X_WITHIN_Y_WEEKS | Any medication corresponding to categories in anti-cancer medication list*, excluding categories like %X% OR if category name is present in category list **, use category config | Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ WITHIN_X_WEEKS_Y_HALF_LIVES | Any medication corresponding to categories in anti-cancer medication list* within X weeks compared to current date (check note) | Half-lives is currently ignored. Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ EXCL_CATEGORY_X_WITHIN_Y_WEEKS_Z_HALF_LIVES | Any medication corresponding to categories in anti-cancer medication list*, excluding categories like %X% OR if category name is present in category list **, use category config | Half-lives currently ignored. Does not include radiotherapy or surgery, these are separate rules.
WILL_REQUIRE_ANY_ANTICANCER_THERAPY_ DURING_TRIAL | won't be evaluated.

*Anti-cancer medication list includes the following categories: Platinum compounds, Pyrimidine antagonists, Taxanes, Alkylating agents, Cytotoxic antibiotics, Gonadorelin agonists, Gonadorelin antagonists, Monoclonal antibody for malignancies, Protein kinase inhibitors, Anti-androgens, Anti-estrogens, 'Oncolytics, other'. 

**Category list refers to 'categories' in the medication data model, OR one of the additionally defined categories:
1] Chemotherapy: includes all medication categories of Platinum compounds, Pyrimidine antagonists, Taxanes and Alkylating agents
2] Immunotherapy: medication drug names Pembrolizumab, Nivolumab, Ipilimumab, Cemiplimab
3] Endocrine therapy: includes all medication categories of Anti-androgens, Anti-estrogens
4] PARP inhibitors: medication drug names Olaparib, Rucaparib
5] Gonadorelin: Gonadorelin agonists, Gonadorelin antagonists 

Note that for all configured nr of weeks, 2 weeks are subtracted from the latest medication date, since these weeks will pass by anyway. 

##### Rules related to pregnancy / anticonception

Rule | When does a patient pass evaluation?
---|---
IS_BREASTFEEDING | > won't be evaluated
IS_PREGNANT | > won't be evaluated
USES_ADEQUATE_ANTICONCEPTION | > won't be evaluated

##### Rules related to cancer related complication

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_COMPLICATION_X | Cancer related complication > name like %X%. Resolves to 'Undetermined' in case UNKNOWN.

##### Rules related to toxicity

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_TOXICITY_OF_AT_LEAST_GRADE_X | Toxicities > grade => X. 
HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y | Toxicities > grade => X and name like %Y%
HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y | Toxicities > grade => X and ignoring name like %Y%. | Multiple names can be specified within 1 rule, separated by ";"
HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS | T.B.D. 

Note for all TOXICITY rules: In case X = 0, 1 or 2, all names corresponding to 'source = Questionnaire' are included (also if 'grade' is unknown), since toxicities are only noted in questionnaire when grade => 2.
In case X = 3 or 4, the evaluation resolves to 'undetermined' if there are names for which grade is not specified.

##### Rules related to vital function / body weight measurements

Rule | When does a patient pass evaluation? 
---|---
HAS_SBP_MMHG_OF_AT_LEAST_X | vitalFunction > Up to 5 most recent systolic blood pressure AND average value => X
HAS_DBP_MMHG_OF_AT_LEAST_X | vitalFunction > Up to 5 most recent diastolic blood pressure AND average value => X
HAS_PULSE_OXYMETRY_OF_AT_LEAST_X | vitalFunction > Up to 5 most recent SpO2 measurements (in percent) AND average value => X
HAS_BODY_WEIGHT_OF_AT_LEAST_X | bodyWeight > Latest body weight measurement (in kg) => X

For SBP, DBP and Pulse oximetry, evaluation should resolve to UNDETERMINED in case of no PASS, but at least 1 of the up to 5 most recent values would be sufficient to PASS.

##### Rules related to blood transfusions

Rule | When does a patient pass evaluation?
---|---
HAS_HAD_ERYTHROCYTE_TRANSFUSION_ WITHIN_LAST_X_WEEKS | Blood transfusions > product = Erythrocyte concentrate AND current date minus transfusion date <= X weeks
HAS_HAD_THROMBOCYTE_TRANSFUSION_ WITHIN_LAST_X_WEEKS | Blood transfusions > product = Thrombocyte concentrate AND current date minus transfusion date <= X weeks

##### Rules related to surgery

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_HAD_RECENT_SURGERY | Surgeries > presence
HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS | Surgeries > Current date minus latest surgery date <= X weeks | Note that X is the protocol nr of weeks. Therefore 2 weeks are subtracted from the latest surgery date.

##### Rules related to smoking
 
Rule | When does a patient pass evaluation?
---|---
HAS_SMOKED_WITHIN_X_MONTHS | Currently resolves to undetermined
 
### Disease Ontology ID (DOID)
 
For rules about e.g. primary tumor location and type, second primaries and 'other conditions', one or more DOIDs may be implemented. For more information, see https://disease-ontology.org/.
 
### Version History and Download Links
 - Upcoming (first release) 