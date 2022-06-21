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
 
An optional flag `run_historically` can be added in which case the treatment matcher sets the date to 3 weeks after the 
patient's registration date. If this flag is not set, the treatment matcher uses the current date as reference date.
 
### Treatment matching

Every treatment defined in the treatment database is evaluated independently. 

In case a treatment is a trial, all relevant inclusion and exclusion criteria are evaluated for this trial as well as every criterion 
for any specific cohort within this trial.  

Every criterion evaluates to one of the following options:

Evaluation | Description
---|---
PASS | The patient complies with the inclusion or exclusion criterion.  
WARN | The patient has a condition that makes complying with inclusion or exclusion criterion unclear. A manual evaluation is required.
FAIL | The patient does not comply with the inclusion or exclusion criterion. 
UNDETERMINED | The data provided to the inclusion or exclusion criterion is insufficient for determining eligibility.
NOT_EVALUATED | The evaluation of the inclusion or exclusion criterion is skipped and can be assumed to be irrelevant for determining trial eligibility. 
NOT_IMPLEMENTED | No algo has been implemented yet for this criterion.

#### Criteria evaluation feedback / Recoverable status

Every criterion algorithm also provides human-readable feedback ('messages') about its evaluation, so that a human can easily and quickly understand which 
evaluation has been done and why the outcome of the evaluation (`PASS`,`WARN`, `FAIL`, `UNDETERMINED` or `NOT_EVALUATED`) is as it is. 

Finally, each criterion algorithm is configured as 'recoverable' or 'unrecoverable', indicating whether or not the outcome of the criterion evaluation 
could be recovered in case of a `FAIL`. For example, lab values may be insufficient at moment of evaluation, but turn out to be sufficient in 2 weeks
when a new lab test is done ('recoverable'), while a tumor type cannot change ('unrecoverable'). 

#### Treatment eligibility

Once all criteria are evaluated, the following algorithm determines whether a patient is potentially eligible for a trial:
 1. For every cohort within a trial, the patient is considered potentially eligible for that cohort in case none of the cohort-specific 
 criteria evaluated to unrecoverable `FAIL` or `NOT_IMPLEMENTED`.
 1. A patient is eligible for a trial in case none of its overall criteria evaluated to unrecoverable `FAIL` or `NOT_IMPLEMENTED` and the trial 
 either has no cohorts defined or has at least one cohort that is considered potentially eligible.

Note that, following this logic, a patient is only considered potentially eligible for a cohort if both the cohort is considered  eligible 
_and_ the trial that the cohort is part of is considered eligible.
   
#### Criteria algorithms

Inclusion and exclusion criteria can be defined as a set of rules that are combined using composite functions, to determine overall eligibility. 

The following composite functions are available:

Function | Description 
---|---
AND | indicates that all combined rules should PASS in order to PASS
OR | indicates that one of the combined rules should PASS in order to PASS
NOT | indicates that the rule should not be PASS in order to PASS
WARN_IF | indicates that a warning should be displayed in case evaluation leads to PASS, thereby resolving to WARN

Some rules require 1 ("X") or more ("X" and "Y") additional configuration parameter(s) that can be set to match the requirements of each trial. 
Also, note that some inclusion and exclusion criteria can be mapped to rules that are currently explicitly set to PASS or explicitly 
won't be evaluated. 

The following rules are available:

##### Rules related to general characteristics / statements

Rule | When does a patient pass evaluation? | Note
---|---|---
IS_AT_LEAST_X_YEARS_OLD | Current year minus birth year > X | `UNDETERMINED` in case of exactly X
IS_MALE | Patient > Gender = Male
HAS_WHO_STATUS_OF_AT_MOST_X | WHO <= X
HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X | > Currently resolves to not evaluated
HAS_LANSKY_SCORE_OF_AT_LEAST_X | > Currently resolves to not evaluated
CAN_GIVE_ADEQUATE_INFORMED_CONSENT | > Won't be evaluated
HAS_RAPIDLY_DETERIORATING_CONDITION | > Won't be evaluated
HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS | > Won't be evaluated
HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS | > Won't be evaluated
IS_TREATED_IN_HOSPITAL_X | > Won't be evaluated
WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X | > Currently set to the Netherlands 
IS_LEGALLY_INSTITUTIONALIZED | > Won't be evaluated
IS_INVOLVED_IN_STUDY_PROCEDURES | > Won't be evaluated

##### Rules related to tumor and lesion localization
 
Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_X | Any configured DOID should be equal or be a child of DOID X | In case the sample configured DOID is defined in the list of "Main cancer types" and is a parent of the requested DOID, AND when sample tumor type = empty or 'carcinoma' (without a subtype), the tumor type may actually be correct but the required details were missing in the clinical data. Therefore, in these situations, resolve to `UNDETERMINED`.
HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X | ALL configured DOIDs equal or child of DOID of tumor type X specified, and none of configured DOIDs should be equal or child of DOID 0050686. Resolve to WARN in case ALL configured DOIDs exactly equal to DOID 162 | X can be one of: Carcinoma (DOID 305), Adenocarcinoma (DOID: 299), Squamous cell carcinoma (DOID: 1749), Melanoma (DOID: 1909)
HAS_PROSTATE_CANCER_WITH_SMALL_CELL_HISTOLOGY | DOID equal or child of DOID 7141, or DOID equal or child of DOID 10283 & primary tumor extra details like %Small cell% | `WARN` in case DOID equal or child of DOIDs 2992, or 10283 & 1800, or 10283 & 169. `Undetermined` in case of DOID exactly equal to DOID 10283   
HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE | Won't be evaluated
HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE | Won't be evaluated
HAS_STAGE_X | Tumor details > stage should be X. X can be one of: I, II, III, IIIA, IIIB, IIIC, IV
HAS_ADVANCED_CANCER | Tumor details > stage should be III(A/B/C) or IV
HAS_METASTATIC_CANCER | Tumor details > stage should be IV 
HAS_ANY_LESION | Tumor details > Either hasLiverLesion, hasCnsLesions, hasBrainLesions, hasBoneLesions and/or hasOtherLesions = 1
HAS_LIVER_METASTASES | Tumor details > hasLiverLesions = 1
HAS_KNOWN_CNS_METASTASES | Tumor details > hasCnsLesions = 1 or hasBrainLesions = 1
HAS_KNOWN_ACTIVE_CNS_METASTASES | Tumor details > hasActiveCnsLesions = 1 or hasActiveBrainLesions = 1
HAS_KNOWN_BRAIN_METASTASES | Tumor details > hasBrainLesions = 1
HAS_KNOWN_ACTIVE_BRAIN_METASTASES | Tumor details > hasActiveBrainLesions = 1
HAS_BONE_METASTASES | Tumor details > hasBoneLesions = 1
HAS_BONE_METASTASES_ONLY | Tumor details > hasBoneLesions = 1, while hasLiverLesions, hasBrainLesions, hasCnsLesions and hasLungLesions = 0 or missing, and otherLesions is empty | WARN in case hasBoneLesions = 1 while all others are missing
HAS_LUNG_METASTASES | Tumor details > hasLungLesions = 1
HAS_BIOPSY_AMENABLE_LESION | Presence of WGS (to be further extended)
HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_ X_MONTHS_BEFORE_IC | Presence of WGS (to be extended)
HAS_ASSESSABLE_DISEASE | Tumor details > hasMeasurableDisease = 1, | `UNDETERMINED` in case missing or false
HAS_MEASURABLE_DISEASE | Tumor details > hasMeasurableDisease = 1 
HAS_MEASURABLE_DISEASE_RECIST | Tumor details > hasMeasurableDisease = 1. | Resolve to WARN in case of tumor type equal or belonging to DOID 2531, 1319, 0060058, 9538
HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA | Currently resolves to undetermined
HAS_INJECTION_AMENABLE_LESION | Currently resolves to undetermined
HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION | Currently resolves to undetermined
HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI | Currently resolves to undetermined
HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT | Currently resolves to undetermined
HAS_SUPERSCAN_BONE_SCAN | Won't be evaluated

##### Rules related to previous cancer treatments

Rule | When does a patient pass evaluation? | Note
---|---|---
IS_ELIGIBLE_FOR_TREATMENT_WITH_ CURATIVE_INTENT | Currently resolves to not evaluated
IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X | Currently resolves to undetermined
HAS_EXHAUSTED_SOC_TREATMENTS | Currently resolves to undetermined
HAS_HAD_AT_LEAST_X_ APPROVED_TREATMENT_LINES | Currently resolves to undetermined, unless there is no prior treatment history and X>0, then resolve to FAIL
HAS_HAD_AT_LEAST_X_SYSTEMIC_ TREATMENT_LINES | Prior tumor treatments > minimal nr of lines in case systemic = 1 => X | 'Minimal' refers to the number of distinct lines (by name). In case minimal nr of lines does not meet the requirements but maximal does, resolve to `UNDETERMINED`
HAS_HAD_AT_MOST_X_SYSTEMIC_ TREATMENT_LINES | Prior tumor treatments > maximal nr of lines in case systemic = 1 <= X | 'Maximal' refers to the total number of lines. In case maximal nr of lines does not meet the requirements but minimal does, resolve to `UNDETERMINED`
HAS_HAD_TREATMENT_NAME_X | Prior tumor treatments > name contains X
HAS_HAD_CATEGORY_X_TREATMENT | Patient has had treatment of category X according to described in 1] below | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_ OF_TYPES_Y | Patient has had treatment of category X according to described in 2] below, and corresponding type like any %Y% | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_ OF_TYPES_Y_WITHIN_Z_WEEKS | Patient has had treatment of category X according to described in 2] below, and corresponding type like any %Y%, with startDate within Z weeks | If no startDate configured, or startDate is not conclusive, resolve to `Undetermined`. Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_ IGNORING_TYPES_Y | Patient has had treatment of category X according to described in 2] below, and corresponding type not like any %Y% | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_AND_ AT_LEAST_Y_LINES | Patient has had treatment of category X according to described in 1] below and number of lines => Y | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_AND_ AT_MOST_Y_LINES | Patient has had treatment of category X according to described in 1] below and number of lines <= Y | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_ AND_AT_LEAST_Z_LINES | Patient has had treatment of category X according to described in 2] below, corresponding type like any %Y% and number of lines => Z | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_ AND_AT_MOST_Z_LINES | Patient has had treatment of category X according to described in 2] below, corresponding type like any %Y% and number of lines <= Z  | Also see 'Notes' below
HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_ WITH_STOP_REASON_PD | Patient has had any treatment of category X with at least one stop reason due to PD | `Undetermined` in case stop reason is unknown but category X treatment is received
HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT | Currently resolves to undetermined
IS_PARTICIPATING_IN_ANOTHER_TRIAL | Won't be evaluated

1] 'Category' can be one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Radiotherapy, Surgery, Transplantation, Trial, Antiviral therapy, Vaccine, Car T, TCR T,  Gene therapy, Supportive treatment.

In addition, 3 following 'Categories' can be assigned:
- Taxane - Treatment names: Paclitaxel, Docetaxel, Cabazitaxel
- Fluoropyrimidine - Treatment names: Capecitabine, Carmofur, Doxifluridine, Fluorouracil, Tegafur
- Tyrosine kinase inhibitors - Category = 'Targeted therapy' and (T.B.D.)
- Nonsteroidal anti-androgen - Treatment names: Flutamide, Nilutamide, Bicalutamide, Enzalutamide, Darolutamide, Ketodarolutamide, Apalutamide

2] 'Category' with specified 'type' can be only one of: Chemotherapy, Hormone therapy, Immunotherapy, Targeted therapy, Radiotherapy, Transplantation, Trial, Car T, Supportive treatment ; since these have a corresponding type in treatment model. For type, multiple types can be specified within one rule, separated by ";"

Notes:
- For category Taxane & Fluoropyrimidine, in case only 'Chemotherapy' configured (without further details), resolve to `UNDETERMINED`
- For category Tyrosine kinase inhibitors, in case only 'Targeted therapy' configured (without further details), resolve to `UNDETERMINED`
- For category Nonsteroidal anti-androgen, in case only 'Hormone therapy' configured (without further details), resolve to `UNDETERMINED`
- For all rules asking for categories with specified type (2]), if only the requested category configured (without further details), resolve to `UNDETERMINED`
- TODO; For all rules asking for specific treatment or categories, in case the treatment line of interest also contains "Trial" and without this line the evaluation does not resolve to PASS, resolve to `UNDETERMINED`

##### Rules related to prior primary tumors

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_ACTIVE_SECOND_MALIGNANCY | Prior second primary > any entry with active=1
HAS_HISTORY_OF_SECOND_MALIGNANCY | Prior second primary > any entry
HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X | Prior second primary > contains any entry with DOID belonging to DOID X
HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS | Prior second primary > current year (+month) - lastTreatmentYear (+month) should be <= X | In case lastTreatmentYear is empty, but diagnosedYear is not, use diagnosedYear - but set X to X+1 to be certain to collect all cases. In case no dates are provided, resolve to UNDETERMINED.

##### Rules related to molecular results

Rule | When does a patient pass evaluation? | Note
---|---|---
ACTIVATION_OR_AMPLIFICATION_OF_GENE_X | Activating mutation or amplification is found in gene X
INACTIVATION_OF_GENE_X | Inactivating mutation or deletion/disruption is found in gene X
ACTIVATING_MUTATION_IN_GENE_X | Activating mutation is found in gene X
MUTATION_IN_GENE_X_OF_TYPE_Y | Specific mutation Y is found in gene X
AMPLIFICATION_OF_GENE_X | Amplification is found in gene X
FUSION_IN_GENE_X | High driver fusion with fusion partner gene X is found
WILDTYPE_OF_GENE_X | No driver mutation or fusion is found in gene X
MSI_SIGNATURE | MS Status = MSI
HRD_SIGNATURE | HR Status = HRD
TMB_OF_AT_LEAST_X | Tumor Mutational Burden (TMB) should be => X
TML_OF_AT_LEAST_X | Tumor Mutational Load (TML) should be => X
TML_OF_AT_MOST_X | TML should be <= X
HAS_HLA_A_TYPE_X | HLA-A type should be X. Currently set to fail (T.B.D.)
OVEREXPRESSION_OF_GENE_X | Currently set to fail (T.B.D.)
NON_EXPRESSION_OF_GENE_X | Currently set to fail (T.B.D.)
EXPRESSION_OF_GENE_X_BY_IHC | Prior molecular test > Test = IHC, Item = X and (scoreText = positive or scoreValue>0)
EXPRESSION_OF_GENE_X_BY_IHC_OF_EXACTLY_Y | Prior molecular test > Test = IHC, Item = X and scoreValue = Y | In case scoreText = "positive" or "negative", resolve to `UNDETERMINED`
EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y | Prior molecular test > Test = IHC, Item = X and scoreValue => Y | In case scoreText = "positive" or "negative", resolve to `UNDETERMINED`
PD_L1_SCORE_CPS_OF_AT_LEAST_X | Prior molecular test > Test = IHC, Item = PD-L1, measure = CPS, scoreValue => X
PD_L1_SCORE_CPS_OF_AT_MOST_X | Prior molecular test > Test = IHC, Item = PD-L1, measure = CPS, scoreValue <= X
PD_L1_SCORE_TPS_OF_AT_MOST_X | Prior molecular test > Test = IHC, Item = PD-L1, measure = TPS (in %), scoreValue <= X
HAS_PSMA_POSITIVE_PET_SCAN | Currently resolves to undetermined
MOLECULAR_RESULTS_MUST_BE_AVAILABLE | Currently set to ingestion of molecular results (T.B.D.)
MOLECULAR_RESULTS_MUST_BE_AVAILABLE_ FOR_GENE_X | Ingestion of molecular results with experiment type 'WGS', or presence of previous molecular test for gene X
MANUFACTURED_T_CELLS_ARE_WITHIN_SHELF_LIFE | Won't be evaluated

##### Rules related to recent laboratory measurements
_Blood components / blood cell components_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X | Leukocytes absolute (LEUKO-ABS) in 10^9/L => X |
HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X | Leukocytes absolute (LEUKO-ABS) in 10^9/L => X*LLN |
HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X | Lymphocytes absolute (LYMPHO-ABS-eDA/LYMPHO-ABS-eDM) in 10*9/L => X
HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X | Lymphocytes in cells per mm3 => X | In case lymphocytes is measured in 10\*9/L, the value is converted using Lympho[cells/mm3]=Lympho[10\*9/L]/0.001
HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X | Neutrophil granulocytes absolute (NEUTRO-ABS/NEUTRO-ABS-eDA) in 10^9/L or 10\*9/L => X 
HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X | Thrombocytes absolute (THROMBO-ABS) in 10*9/L => X 
HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X | Hemoglobin (Hb) in g/dL => X. | In case Hb is measured in mmol/L, the value is converted to g/dL using Hb[g/dL]=Hb[mmol/L]/0.6206
HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X | Hemoglobin (Hb) in mmol/L => X. | In case Hb is measured in g/dL, the value is converted to mmol/L using Hb[mmol/L]=Hb[g/dL]*0.6206

_Blood clotting factors_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_INR_ULN_OF_AT_MOST_X | International normalized ratio (INR/POCT_INR) <= X*ULN 
HAS_PT_ULN_OF_AT_MOST_X | Prothrombin time (PT/POCT_PT) <= X*ULN 
HAS_APTT_ULN_OF_AT_MOST_X | Activated partial thromboplastin time (APTT) <= X*ULN 
HAS_PTT_ULN_OF_AT_MOST_X | T.B.D. 
HAS_D_DIMER_OUTSIDE_REF_UPPER_LIMIT | D-dimer (DDIM) > refLimitUp

_Liver function_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X | Albumin (ALB) in g/dL => X. | In case ALB is measured in g/L, the value is converted using ALB[g/dL]=ALB[g/L]/10.
HAS_ALBUMIN_LLN_OF_AT_LEAST_X | Albumin (ALB) > X*ULN
HAS_ASAT_ULN_OF_AT_MOST_X | Aspartate aminotransferase (ASAT) <= X*ULN 
HAS_ALAT_ULN_OF_AT_MOST_X | Alanine aminotransferase (ALAT) <= X*ULN
HAS_ALP_ULN_OF_AT_MOST_X | Alkaline phosphatase (ALP) <= X*ULN
HAS_TOTAL_BILIRUBIN_ULN_ OF_AT_MOST_X | Total Bilirubin (TBIL) <= X*ULN
HAS_TOTAL_BILIRUBIN_UMOL_ PER_L_OF_AT_MOST_X | Total Bilirubin (TBIL) in umol/L <= X
HAS_DIRECT_BILIRUBIN_ULN_ OF_AT_MOST_X | Direct Bilirubin (DBIL) <= X*ULN 
HAS_DIRECT_BILIRUBIN_PERCENTAGE_ OF_TOTAL_OF_AT_MOST_X | Direct Bilirubin (DBIL) / Total Bilirubin (TBIL) * 100 <= X

_Kidney function_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X | Creatinine (CREA) in mg/dL <= X | In case CREA is measured in umol/l, the value is converted using CREA[mg/dL]=CREA[umol/l]/88.42
HAS_CREATININE_ULN_OF_AT_MOST_X | Creatinine (CREA) <= X*ULN 
HAS_EGFR_CKD_EPI_OF_AT_LEAST_X | eGFR (CKD-EPI formula) => X. | In case CrCl is measured in another unit, the value is converted using
HAS_EGFR_MDRD_OF_AT_LEAST_X | eGFR (MDRD formula) => X. | In case CrCl is measured in another unit, the value is converted using
HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X | Creatinine clearance (Cockcroft Gault formula) => X. | In case CrCl is measured in another unit, the value is converted using

_Cardiac / cardiovascular function_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_BNP_ULN_OF_AT_MOST_X | NT-pro-BNP (BNP) <= X*ULN
HAS_TROPONIN_IT_ULN_OF_AT_MOST_X | High-sensitivity Troponin T (HSTNT) <= X*ULN
HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X | Triglyceride (TG) <= X

_Pancreas function_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_AMYLASE_ULN_OF_AT_MOST_X | Amylase (AMYL) <= X*ULN 
HAS_LIPASE_ULN_OF_AT_MOST_X | Lipase (LIPA) <= X*ULN 

_Minerals_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X | Calcium (Ca) in mg/dL <= X | In case calcium is measured in mmol/L, the value is converted to mg/dL using Ca[mg/dL]=Ca[mmol/L]/0.2495
HAS_CALCIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Calcium (Ca) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X | Calcium corrected (Ca_C) <= X*ULN
HAS_CORRECTED_CALCIUM_WITHIN_ INSTITUTIONAL_NORMAL_LIMITS | Calcium corrected (Ca_C) LLN<X<ULN (isOutsideRef=0)
HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X | Iononized calcium (B_ICA) in mmol/L <= X
HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Magnesium (MG) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)
HAS_PHOSPHORUS_ULN_OF_AT_MOST_X | Phosphate (P) <= X*ULN
HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Phosphate (P) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)
HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X | Potassium (K) in mmol/l => X
HAS_POTASSIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | Potassium (K) LLN<X<ULN (isOutsideRef=0)
HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_ NORMAL_LIMITS | T.B.D. LLN<X<ULN (isOutsideRef=0)

_Hormones_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_SERUM_TESTOSTERONE_NG_PER_DL_ OF_AT_MOST_X | Serum testosterone (T.B.D.) in ng/dL <= X

_Tumor markers_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_AFP_ULN_OF_AT_LEAST_X | Alpha fetoprotein (AFP) <= X*ULN
HAS_CA125_ULN_OF_AT_LEAST_X | CA 125 (C125) <= X*ULN
HAS_HCG_ULN_OF_AT_LEAST_X | HCG + beta HCG (HCG) <= X*ULN
HAS_LDH_ULN_OF_AT_MOST_X | Lactate dehydrogenase (LDH) <= X*ULN
HAS_PSA_UG_PER_L_OF_AT_LEAST_X | Prostate-specific antigen (PSA) => X
HAS_PSA_LLN_OF_AT_LEAST_X | Prostate-specific antigen (PSA) => X*LLN

_Urine measurements_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X | Total protein in urine (TE_U) => X.
HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X | In g/24h. T.B.D

_Other_

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X | Glucose /PL (GL_P) in mmol/L <= X

ULN = Upper Limit of Normal, LLN = Lower Limit of Normal; implemented as refLimitUp and refLimitLow, respectively.

Note: for all lab values, the latest available lab value (up to 30 days) is evaluated. 
If the latest lab value is out of the requested range, the second-last lab value is evaluated. 
In case that a second-last lab value is available and within requested range as configured in the rule, the evaluation resolves to UNDETERMINED. In case no second-last value is applicable, 
or that this value is also out of requested range, the evaluation resolves to FAIL.

##### Rules related to other conditions

Rule | When does a patient pass evaluation?
---|---
HAS_HISTORY_OF_SPECIFIC_CONDITION_ WITH_DOID_X | Prior other conditions > any configured doid should be equal or be a child of DOID "X"
HAS_HISTORY_OF_SPECIFIC_CONDITION_ X_BY_NAME | Prior other conditions > name like %X%
HAS_HISTORY_OF_AUTOIMMUNE_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 417
HAS_HISTORY_OF_BRAIN_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 936
HAS_HISTORY_OF_CARDIAC_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 114
HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 1287
HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 331
HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 77
HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 2914
HAS_HISTORY_OF_LIVER_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 409
HAS_HISTORY_OF_LUNG_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 850
HAS_HISTORY_OF_MYOCARDIAL_INFARCT | Prior other conditions > any configured doid should be equal or be a child of DOID 5844 
HAS_HISTORY_OF_STROKE | Prior other conditions > any configured doid should be equal or be a child of DOID 6713 
HAS_HISTORY_OF_TIA | Prior other conditions > any configured doid should be equal or be a child of DOID 224 
HAS_HISTORY_OF_VASCULAR_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 178
HAS_SEVERE_CONCOMITANT_CONDITION | Won't be evaluated
HAS_HAD_ORGAN_TRANSPLANT | Prior other conditions > categories contains "Organ transplant"
HAS_GILBERT_DISEASE | Prior other conditions > any configured doid should be equal or be a child of DOID 2739
HAS_HYPERTENSION | Prior other conditions > any configured doid should be equal or be a child of DOID 10763
HAS_HYPOTENSION | Prior other conditions > name like %hypotension%
HAS_DIABETES | Prior other conditions > any configured doid should be equal or be a child of DOID 9351
HAS_POTENTIAL_ABSORPTION_DIFFICULTIES | Or: Prior other condition belonging to DOID 77, Complication of name %Diarrhea%, %Nausea%, %Small bowel resection%, %Colectomy%, %Vomit%, Toxicity source questionnaire or EHR grade=>2 of name %Diarrhea%, %Nausea%, %Vomit%
HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES | Or: Has complication of name %tube%, %swallow% (T.B.D.)
HAS_POTENTIAL_CONTRAINDICATION_TO_CT | > prior other condition > name like %claustrophobia% or any other condition belonging to DOID 557, or intolerance name like %contrast agent%, or pregnancy, or medication use of name %metformin%, or complication of name %hyperthyroidism%
HAS_POTENTIAL_CONTRAINDICATION_TO_MRI | > prior other condition > category like %Implant% or name like %Claustrophobia% or any other condition belonging to DOID 557, or intolerance name like %contrast agent%
HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI | > Same contraindications as for HAS_POTENTIAL_CONTRAINDICATION_TO_MRI, and: (T.B.D.)
IS_IN_DIALYSIS | Won't be evaluated
HAS_ADEQUATE_VEIN_ACCESS_FOR_LEUKAPHERESIS | Currently resolves to undetermined

##### Rules related to cardiac function

Rule | When does a patient pass evaluation?
---|---
HAS_CARDIAC_ARRHYTHMIA | Clinical status > hasSigAberrationLatestECG = 1
HAS_LVEF_OF_AT_LEAST_X | clinicalStatus > lvef should be => x. Unavailable LVEF data leads to UNDETERMINED, out of range LVEF leads to FAIL
HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN | clinicalStatus > lvef should be => X. Unavailable LVEF data leads to PASS, out of range LVEF leads to FAIL
HAS_QTC_OF_AT_MOST_X | QTcF or QTcB. Currently: Clinical status > qtcfValue in ms <= X
HAS_QTCF_OF_AT_MOST_X | Clinical status > qtcfValue in ms <= X
HAS_LONG_QT_SYNDROME | Prior other conditions > any configured doid should be equal or be a child of DOID 2843

##### Rules related to infections

Rule | When does a patient pass evaluation?
---|---
HAS_ACTIVE_INFECTION | Clinical status > hasActiveInfection = 1
HAS_KNOWN_EBV_INFECTION | Prior other conditions > name like %EBV% or %Epstein Barr%
HAS_KNOWN_HEPATITIS_A_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 12549
HAS_KNOWN_HEPATITIS_B_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 2043
HAS_KNOWN_HEPATITIS_C_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 1883
HAS_KNOWN_HIV_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 526
HAS_KNOWN_HTLV_INFECTION | Prior other conditions > name like %HTLV%
HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION |  Prior other conditions > configured doid should be equal or be a child of DOID 0080827
HAS_KNOWN_TUBERCOLOSIS_INFECTION | Prior other conditions > configured doid should be equal or be a child of DOID 399
MEETS_COVID_19_INFECTION_REQUIREMENTS | Currently resolves to undetermined
IS_FULLY_VACCINATED_AGAINST_COVID_19 | Currently resolves to undetermined
ADHERENCE_TO_PROTOCOL_REGARDING_ ATTENUATED_VACCINE_USE | Won't be evaluated

##### Rules related to current medication 

Rule | When does a patient pass evaluation?| Note
---|---|---
CURRENTLY_GETS_NAME_X_MEDICATION | Medication > name like %X% and status is active
CURRENTLY_GETS_CATEGORY_X_MEDICATION | Medication > categories like %X% and status is active
HAS_RECEIVED_CATEGORY_X_MEDICATION_ WITHIN_Y_WEEKS | Medication > categories like %X% and active OR stopDate within Y weeks | Undetermined in case Y would require stop dates to be prior to ACTIN registration date.
CURRENTLY_GETS_ANTICOAGULANT_MEDICATION | Medication > categories contains type of "Anticoagulants" or "Vitamin K antagonists" and status is active
CURRENTLY_GETS_AZOLE_MEDICATION | Medication > categories contains type of "Triazoles" or "Imidazoles, cutaneous" or "Imidazoles, other" and status is active
CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION | Medication > categories contains type of "Bisphosphonates" or "Calcium regulatory medication" and status is active
CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION | Medication > categories contains type of "Vitamin K Antagonists" and status is active
CURRENTLY_GETS_GONADORELIN_MEDICATION | Medication > categories contains type of "Gonadorelin antagonists" or "Gonadorelin agonists" and status is active
CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION | Medication > categories contains type of "Immunosuppressants, selective" or "Immunosuppresants, other" and status is active 
CURRENTLY_GETS_PROHIBITED_MEDICATION | T.B.D., currently resolves to undetermined
CURRENTLY_GETS_POTENTIALLY_QT_ PROLONGATING_MEDICATION | T.B.D., currently resolves to undetermined
CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X | T.B.D., currently resolves to undetermined | Cytochrome P450 enzymes
CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X | T.B.D., currently resolves to undetermined
HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS | T.B.D., currently resolves to undetermined
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_CYP_X | T.B.D., currently resolves to undetermined 
CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X | T.B.D., currently resolves to undetermined  
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_PGP | T.B.D., currently resolves to undetermined | P-glycoprotein
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_BCRP | T.B.D., currently resolves to undetermined | Breast cancer resistance protein
CURRENTLY_GETS_MEDICATION_INHIBITING_OR_ INDUCING_DRUG_METABOLIZING_ENZYMES | Currently resolves to warn in case patient receives any medication
HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING | Medication > categories contains "Anticoagulants" AND only 1 distinct dosage (T.B.D)
IS_WILLING_TO_TAKE_PREMEDICATION | Currently won't be evaluated

##### Rules related to washout period 

Rule | When does a patient pass evaluation?| Note
---|---|---
HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_ WITHIN_Y_WEEKS | medication > any names like %X% and active Y weeks prior to evaluation date | 
HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_ WITHIN_Y_WEEKS_Z_HALF_LIVES | medication > any names like %X% and active Y weeks prior to evaluation date and Z half lives | Half-lives is currently ignored.
HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_ WITHIN_Y_WEEKS | medication > any categories like %X% OR if category name is present in category list **, use category config ; active Y weeks prior to evaluation | 
HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_ WITHIN_Y_WEEKS_Z_HALF_LIVES | medication > any categories like %X% OR if category name is present in category list **, use category config ; active Y weeks prior to evaluation and Z half lives | Half-lives is currently ignored.
HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS | Radiotherapy in treatment history when: 1] no date provided; 2] in case only a year is provided then in case of current year; 3] in case year+month is provided then in case of current year and current month | 
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ WITHIN_X_WEEKS | Any medication corresponding to categories in anti-cancer medication list* active X weeks prior to evaluation date | Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ EXCL_CATEGORIES_X_WITHIN_Y_WEEKS | Any medication corresponding to categories in anti-cancer medication list*, excluding categories like %X% OR if category name is present in category list **, use category config | Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ WITHIN_X_WEEKS_Y_HALF_LIVES | Any medication corresponding to categories in anti-cancer medication list* active X weeks prior to evaluation | Half-lives is currently ignored. Does not include radiotherapy or surgery, these are separate rules.
HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_ EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES | Any medication corresponding to categories in anti-cancer medication list*, excluding categories like %X% OR if category name is present in category list **, use category config | Half-lives currently ignored. Does not include radiotherapy or surgery, these are separate rules.
WILL_REQUIRE_ANY_ANTICANCER_THERAPY_ DURING_TRIAL | won't be evaluated.
HAS_RECEIVED_HERBAL_MEDICATION_OR_DIETARY_ SUPPLEMENTS_WITHIN_X_WEEKS | medication > categories like %supplements% or %herbal remedy% active X weeks prior to evaluation date

*Anti-cancer medication list includes the following categories: categories like %Platinum compound%, %Pyrimidine antagonist%, %Taxane%, %Alkylating agent%, %Cytotoxic antibiotics%, %Gonadorelin agonist%, %Gonadorelin antagonist%, %Monoclonal antibody for malignancies%, %Protein kinase inhibitor%, %Anti-androgen%, %Anti-estrogen%, '%Oncolytics, other%'. 

**Category list refers to 'categories' in the medication data model, OR one of the additionally defined categories:<br>
1] Chemotherapy: includes all medication categories like %Platinum compound%, %Pyrimidine antagonist%, %Taxane% and %Alkylating agent%  
2] Immunotherapy: medication drug names Pembrolizumab, Nivolumab, Ipilimumab, Cemiplimab  
3] Endocrine therapy: includes all medication categories like %Anti-androgen%, %Anti-estrogen%  
4] PARP inhibitors: medication drug names Olaparib, Rucaparib  
5] Gonadorelin: includes medication categories like %Gonadorelin agonist%, %Gonadorelin antagonist%   
6] Immunosuppressants: includes medication categories like %Immunosuppressants, selective%, %Immunosuppressants, other%   

Note that for all configured nr of weeks, 2 weeks are subtracted from the latest medication date, since these weeks will pass by anyway. 

##### Rules related to reproduction

Rule | When does a patient pass evaluation?
---|---
IS_BREASTFEEDING | Applicable only for women. Won't be evaluated
IS_PREGNANT | Applicable only for women. Won't be evaluated
USES_ADEQUATE_ANTICONCEPTION | Won't be evaluated
ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS | Won't be evaluated

##### Rules related to complications

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_COMPLICATION_X | complication > Name like %X%
HAS_UNCONTROLLED_TUMOR_RELATED_PAIN | complication > Name like %pain% or current use of medication with name Hydromorphone (T.B.D.)
HAS_LEPTOMENINGEAL_DISEASE | complication > Name like %leptomeningeal%disease% or %leptomeningeal%metastases% or %carcinomatous%meningitis%. | Warn in case of hasCnsLesions=1 or otherLesions like %leptomeningeal% or %carcinomatous%meningitis%
HAS_SPINAL_CORD_COMPRESSION | complication > Name like %spinal%cord%compression% or %cervical%spondylotic%myelopathy% 
HAS_URINARY_INCONTINENCE | complication > Name like %incontinence% or %bladder%control% 
HAS_BLADDER_OUTFLOW_OBSTRUCTION | complication > Name like %bladder%outflow% or %bladder%outlet% or %bladder%obstruction% or %bladder%retention%, or prior other condition belonging to DOID 13948

##### Rules related to intolerances/toxicities

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_INTOLERANCE_FOR_NAME_X | Intolerance > Name like %X%
HAS_INTOLERANCE_BELONGING_TO_DOID_X | Intolerance > doid is equal of a child of doid X
HAS_INTOLERANCE_FOR_TAXANE | Intolerance > Name contains any taxane | Taxanes: Paclitaxel, Docetaxel, Cabazitaxel
HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION | Intolerance > Resolves to undetermined in case of presence of any active allergies belonging to categories in 'Medication' and/or DOID 0060500 
HAS_HISTORY_OF_ANAPHYLAXIS | Resolves to undetermined in case of presence of any allergies (T.B.D.)
HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS | Resolves to undetermined in case of previous treatment with categories like immunotherapy
HAS_TOXICITY_OF_AT_LEAST_GRADE_X | Toxicities > grade => X
HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y | Toxicities > grade => X and name like %Y%
HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y | Toxicities > grade => X and ignoring name like %Y%. | Multiple names can be specified within 1 rule, separated by ";"

Note for all TOXICITY rules: In case X = 0, 1 or 2, all names corresponding to 'source = Questionnaire' are included (also if 'grade' is unknown), since toxicities are only noted in questionnaire when grade => 2.
In case X = 3 or 4, the evaluation resolves to 'undetermined' if there are names for which grade is not specified.

##### Rules related to vital function / body weight measurements

Rule | When does a patient pass evaluation? 
---|---
HAS_SBP_MMHG_OF_AT_LEAST_X | vitalFunction > Include measurements up to 5 different days but must be within a month, with over all average systolic blood pressure value => X
HAS_SBP_MMHG_OF_AT_MOST_X | vitalFunction > Include measurements up to 5 different days but must be within a month, with over all average systolic blood pressure value <= X
HAS_DBP_MMHG_OF_AT_LEAST_X | vitalFunction > Include measurements up to 5 different days but must be within a month, with over all average diastolic blood pressure value => X
HAS_DBP_MMHG_OF_AT_MOST_X | vitalFunction > Include measurements up to 5 different days but must be within a month, with over all average diastolic blood pressure value <= X
HAS_PULSE_OXIMETRY_OF_AT_LEAST_X | vitalFunction > Up to 5 most recent SpO2 measurements (in percent) AND median value => X
HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y | Vital function > Up to 5 most recent HR measurements (in BPM) AND average value between X and Y 
HAS_BODY_WEIGHT_OF_AT_LEAST_X | bodyWeight > Latest body weight measurement (in kg) => X

For SBP, DBP and Pulse oximetry, evaluation should resolve to UNDETERMINED rather than FAIL in case of no PASS, but at least 1 of the up to 5 most recent values would be sufficient to PASS.

##### Rules related to blood transfusions

Rule | When does a patient pass evaluation?
---|---
HAS_HAD_ERYTHROCYTE_TRANSFUSION_ WITHIN_LAST_X_WEEKS | Blood transfusions > product = Erythrocyte concentrate AND current date minus transfusion date <= X weeks
HAS_HAD_THROMBOCYTE_TRANSFUSION_ WITHIN_LAST_X_WEEKS | Blood transfusions > product = Thrombocyte concentrate AND current date minus transfusion date <= X weeks

##### Rules related to surgery

Rule | When does a patient pass evaluation? | Note
---|---|---
HAS_HAD_RECENT_SURGERY | Surgeries > presence of surgery entry within 2 months
HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS | Surgeries > Current date minus latest surgery date <= X weeks | Note that X is the protocol nr of weeks. Therefore 2 weeks are subtracted from the latest surgery date.

##### Rules related to lifestyle
 
Rule | When does a patient pass evaluation?
---|---
IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES | Resolves to WARN
 
### Disease Ontology ID (DOID)
 
For rules about e.g. primary tumor location and type, second primaries and 'other conditions', one or more DOIDs may be implemented. For more information, see https://disease-ontology.org/.
 
### Version History and Download Links
 - Upcoming (first release) 