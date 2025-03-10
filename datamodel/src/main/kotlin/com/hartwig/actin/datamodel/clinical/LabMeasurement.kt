package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class LabMeasurement(val display: String, val defaultUnit: LabUnit) : Displayable {
    ANGIOTENSIN_CONVERTING_ENZYME_2("ACE-2", LabUnit.UNITS_PER_LITER),
    ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME_RATIO("APTT ratio", LabUnit.NONE),
    ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME("APTT", LabUnit.SECONDS),
    ADRENOCORTICOTROPIC_HORMONE("ACTH", LabUnit.PICOMOLES_PER_LITER),
    ALANINE_AMINOTRANSFERASE("ALAT", LabUnit.UNITS_PER_LITER),
    ALBUMIN_CREATININE_RATIO("Albumin/Creatinine ratio", LabUnit.GRAMS_PER_MOLE),
    ALBUMIN_FRACTION_C("Albumin fraction C", LabUnit.GRAMS_PER_LITER),
    ALBUMIN_URINE("Albumin (urine)", LabUnit.GRAMS_PER_LITER),
    ALBUMIN("Albumin", LabUnit.GRAMS_PER_LITER),
    ALKALINE_PHOSPHATASE("ALP", LabUnit.UNITS_PER_LITER),
    ALPHA_1_ANTITRYPSIN("Alpha 1 antitrypsin", LabUnit.GRAMS_PER_LITER),
    ALPHA_1_MICROGLOBULIN_CREATININE_RATIO_URINE("A1M/creatinine ratio (urine)", LabUnit.GRAMS_PER_MOLE),
    ALPHA_1_MICROGLOBULIN_URINE("A1M (urine)", LabUnit.MILLIGRAMS_PER_LITER),
    ALPHA_FETOPROTEIN("AFP", LabUnit.NANOGRAMS_PER_MILLILITER),
    ALPHA1_FRACTION_C("Alpha 1 fraction C", LabUnit.GRAMS_PER_LITER),
    ALPHA2_FRACTION_C("Alpha 2 fraction C", LabUnit.GRAMS_PER_LITER),
    AMYLASE("Amylase", LabUnit.UNITS_PER_LITER),
    ANDROSTENEDIONE("Androstenedione", LabUnit.NANOMOLES_PER_LITER),
    ANION_GAP("Anion Gap", LabUnit.MILLIMOLES_PER_LITER),
    APOLIPOPROTEIN_B("Apolipoprotein B", LabUnit.GRAMS_PER_LITER),
    ASPARTATE_AMINOTRANSFERASE("ASAT", LabUnit.UNITS_PER_LITER),
    BASE_EXCESS_BG("Base excess (blood gas)", LabUnit.MILLIMOLES_PER_LITER),
    BASOPHIL_GRANULOCYTES_ABS("Basophil granulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    BASOPHIL_GRANULOCYTES_PCT("Basophil granulocytes percentage", LabUnit.PERCENTAGE),
    BCELL_PCT("Percentage B-cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    BETA_2_MICROGLOBULIN("B2M", LabUnit.MILLIGRAMS_PER_LITER),
    BETA_FRACTION_C("Beta fraction C", LabUnit.GRAMS_PER_LITER),
    BICARBONATE("Bicarbonate", LabUnit.MILLIMOLES_PER_LITER),
    BOUND_TRIIODOTHYRONINE( "Bound T3", LabUnit.NANOMOLES_PER_LITER),
    C_PEPTIDE("C-Peptide", LabUnit.NANOMOLES_PER_LITER),
    CALCITONIN("Calcitonin", LabUnit.NANOGRAMS_PER_LITER),
    CALCIUM("Calcium", LabUnit.MILLIMOLES_PER_LITER),
    CALPROTECTINE_WEIGHT("Weight calprotectine Buhlman", LabUnit.GRAMS),
    CALPROTECTINE("Calprotectine Buhlman", LabUnit.MICROGRAMS_PER_GRAM),
    CARBOHYDRATE_ANTIGEN_125("CA 125", LabUnit.KILOUNITS_PER_LITER),
    CARBOHYDRATE_ANTIGEN_15_3("CA 15.3", LabUnit.KILOUNITS_PER_LITER),
    CARBOHYDRATE_ANTIGEN_19_9("CA 19.9", LabUnit.KILOUNITS_PER_LITER),
    CARCINOEMBRYONIC_ANTIGEN("Carcinoembryonic antigen", LabUnit.MICROGRAMS_PER_LITER),
    CYCLIC_CITRULLINATED_PEPTIDE_AB("CCP antibodies", LabUnit.UNITS_PER_MILLILITER),
    CD4_CD8_RATIO("CD4/CD8 ratio", LabUnit.NONE),
    CD4_POSITIVE_CELLS_PCT("Percentage CD4+ T-cells", LabUnit.PERCENTAGE_OF_T_CELLS),
    CD4_POSITIVE_CELLS_ABS("CD4+T-Cells absolute", LabUnit.MILLIONS_PER_LITER),
    CD8_POSITIVE_CELLS_PCT("Percentage CD8+ T-cells", LabUnit.PERCENTAGE_OF_T_CELLS),
    CD8_POSITIVE_CELLS_ABS("CD8+T-Cells absolute", LabUnit.MILLIONS_PER_LITER),
    CERULOPLASMIN("Ceruloplasmin", LabUnit.GRAMS_PER_LITER),
    CHLORIDE_URINE("Chloride (urine)", LabUnit.MILLIMOLES_PER_LITER),
    CHLORIDE("Chloride", LabUnit.MILLIMOLES_PER_LITER),
    CHOLESTEROL_HDL_RATIO("Cholesterol-HDL ratio", LabUnit.NONE),
    CHOLESTEROL("Cholesterol", LabUnit.MILLIMOLES_PER_LITER),
    CHROMOGRANIN_A("CgA", LabUnit.NANOGRAMS_PER_MILLILITER),
    CREATINE_KINASE_MB("CKMB", LabUnit.MICROGRAMS_PER_LITER),
    CARBOXYHEMOGLOBIN_F("COHb (F)", LabUnit.NONE),
    CARBOXYHEMOGLOBIN_PCT("COHb (%)", LabUnit.PERCENTAGE),
    CARBOXYHEMOGLOBIN("COHb", LabUnit.NONE),
    CORRECTED_CALCIUM("Corrected calcium", LabUnit.MILLIMOLES_PER_LITER),
    CORTISOL_URINE("Cortisol (urine)", LabUnit.NANOMOLES_PER_LITER),
    CORTISOL("Cortisol", LabUnit.NANOMOLES_PER_LITER),
    CREATINE_KINASE("Creatine Kinase", LabUnit.UNITS_PER_LITER),
    CREATININE_24U("Creatinine (24h urine)", LabUnit.MILLIMOLES_PER_DAY),
    CREATININE_CLEARANCE_CG("Creatinine clearance (CG)", LabUnit.NONE),
    CREATININE_CLEARANCE_24H("Creatinine-clearance (24h urine)", LabUnit.MILLILITERS_PER_MINUTE),
    CREATININE_URINE("Creatinine (urine)", LabUnit.MILLIMOLES_PER_LITER),
    CREATININE("Creatinine", LabUnit.MICROMOLES_PER_LITER),
    C_REACTIVE_PROTEIN("CRP", LabUnit.MILLIGRAMS_PER_LITER),
    CT_ALBUMIN("ct_Albumin", LabUnit.NONE),
    CYFRA_21_1("Cyfra 21.1", LabUnit.MICROGRAMS_PER_LITER),
    CYIGK_CYIGL_RATIO("CyIgK/CyIgL ratio", LabUnit.NONE),
    D_DIMER("D-dimer", LabUnit.MILLIGRAMS_PER_LITER),
    DEV_POP_PCT("% deviating population", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    DEHYDROEPIANDROSTERON("DHEA", LabUnit.NANOMOLES_PER_LITER),
    DEHYDROEPIANDROSTERON_SULFATE("DHEA sulfate", LabUnit.MICROMOLES_PER_LITER),
    DIRECT_BILIRUBIN("Direct bilirubin", LabUnit.MICROMOLES_PER_LITER),
    DNA_CONC_A("DNA concentration A", LabUnit.MICROGRAMS_PER_MICROLITER),
    DNA_CONC_B("DNA concentration B", LabUnit.MICROGRAMS_PER_MICROLITER),
    ELEVEN_DESOXYCORTISOL("DOC", LabUnit.NANOMOLES_PER_LITER),
    DSDNA_ANTIBODIES("dsDNA antibodies", LabUnit.INTERNATIONAL_UNITS_PER_MILLILITER),
    EGFR_CKD_EPI("eGFR (CKD-EPI)", LabUnit.MILLILITERS_PER_MINUTE),
    EGFR_MDRD("eGFR (MDRD)", LabUnit.MILLILITERS_PER_MINUTE),
    EOSINOPHIL_GRANULOCYTES_ABS("Eosinophil granulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    EOSINOPHIL_GRANULOCYTES_LQ("Eosinophil granulocytes LQ", LabUnit.MILLIONS_PER_LITER),
    EOSINOPHIL_GRANULOCYTES_PCT("Eosinophil granulocytes percentage", LabUnit.PERCENTAGE),
    ERFM("Rheumatoid factors", LabUnit.INTERNATIONAL_UNITS_PER_MILLILITER),
    ERYTHROBLASTS_ABS("Erythroblasts absolute", LabUnit.BILLIONS_PER_LITER),
    ERYTHROCYTES_LQ("Erythrocytes (liquor)", LabUnit.TRILLIONS_PER_LITER),
    ERYTHROCYTES_ABS("Erythrocytes absolute", LabUnit.TRILLIONS_PER_LITER),
    ERYTHROCYTE_SEDIMENTATION_RATE("ESR", LabUnit.MILLIMETERS_PER_HOUR),
    ESTRADIOL("Estradiol", LabUnit.PICOMOLES_PER_LITER),
    EVEROLIMUS("Everolimus", LabUnit.MICROGRAMS_PER_LITER),
    FVIII_ACTIVITY("FVIII activity", LabUnit.UNITS_PER_MILLILITER),
    FACTOR_V("Factor V", LabUnit.UNITS_PER_MILLILITER),
    FIBRINOGEN("Fibrinogen", LabUnit.GRAMS_PER_LITER),
    FERRITIN("Ferritin", LabUnit.MICROGRAMS_PER_LITER),
    FIVE_HIAA_24U("5-HIAA (24h urine)", LabUnit.NONE),
    FIVE_HIAA_CREATININE_RATIO("5-HIAA creatinine ratio", LabUnit.MILLIMOLES_PER_MOLE),
    FIVE_HIAA_URINE("5-HIAA (urine)", LabUnit.MICROMOLES_PER_LITER),
    FOLIC_ACID("Folic acid", LabUnit.NANOMOLES_PER_LITER),
    FREE_CORTISOL("Free cortisol", LabUnit.NANOMOLES_PER_DAY),
    FREE_KAPPA_FREE_LAMBDA_RATIO("Ratio Free Kappa / Free Lambda", LabUnit.NONE),
    FREE_KAPPA("Free Kappa", LabUnit.MILLIGRAMS_PER_LITER),
    FREE_LAMBDA("Free Lambda", LabUnit.MILLIGRAMS_PER_LITER),
    FREE_PROSTATE_SPECIFIC_ANTIGEN("Free PSA", LabUnit.MICROGRAMS_PER_LITER),
    FREE_TRIIODOTHYRONINE("Free T3", LabUnit.PICOMOLES_PER_LITER),
    FREE_THYROXINE("Free T4", LabUnit.PICOMOLES_PER_LITER),
    FREE_TESTOSTERONE( "Free testosterone", LabUnit.NANOMOLES_PER_LITER),
    FRL("Freeze number leukemia", LabUnit.NONE),
    FOLLICLE_STIMULATING_HORMONE("FSH", LabUnit.UNITS_PER_LITER),
    GAMMA_FRACTION_C("Gamma fraction C", LabUnit.GRAMS_PER_LITER),
    GAMMA_GLUTAMYLTRANSFERASE("GGT", LabUnit.UNITS_PER_LITER),
    GASTRIN("Gastrin", LabUnit.NANOGRAMS_PER_LITER),
    GLA("Mprot IgG-L1", LabUnit.GRAMS_PER_LITER),
    GLUCOSE_LQ("Glucose (liquor)", LabUnit.MILLIMOLES_PER_LITER),
    GLUCOSE("Glucose", LabUnit.MILLIMOLES_PER_LITER),
    GRANULOCYTES_LQ("Granulocytes (liquor)", LabUnit.BILLIONS_PER_LITER),
    GROWTH_HORMONE("Growth hormone", LabUnit.MICROGRAMS_PER_LITER),
    HAPTOGLOBIN("Haptoglobin", LabUnit.GRAMS_PER_LITER),
    FETAL_HEMOGLOBIN_F("HbF (F)", LabUnit.NONE),
    FETAL_HEMOGLOBIN_PCT("HbF (%)", LabUnit.PERCENTAGE),
    HUMAN_CHORIONIC_GONADOTROPIN("HCG", LabUnit.INTERNATIONAL_UNITS_PER_LITER),
    HDL_CHOLESTEROL("HDL", LabUnit.MILLIMOLES_PER_LITER),
    HEMATOCRIT("Hematocrit", LabUnit.NONE),
    HEMOGLOBIN_A0("Hemoglobin A0", LabUnit.PERCENTAGE),
    HEMOGLOBIN_A1C("Hemoglobin A1c", LabUnit.MILLIMOLES_PER_MOLE),
    HEMOGLOBIN_E("Hemoglobin E", LabUnit.PERCENTAGE),
    HEMOGLOBIN("Hemoglobin", LabUnit.MILLIMOLES_PER_LITER),
    HEMOLYSIS_INDEX("Hemolysis index", LabUnit.NONE),
    HFC_LQ("High fluorescent cells in liquor", LabUnit.MILLIONS_PER_LITER),
    ANTI_EXTRACTABLE_NUCLEAR_ANTIGEN("Anti-ENA", LabUnit.NONE),
    ICTERIC_INDEX("Icteric index", LabUnit.NONE),
    IMMUNOGLOBIN_A("IgA", LabUnit.GRAMS_PER_LITER),
    IGF_BP3("IGF-BP3", LabUnit.MILLIGRAMS_PER_LITER),
    INSULIN_LIKE_GROWTH_FACTOR_1_STANDARD_DEVIATION_SCORE("IGF-1 SDS", LabUnit.NONE),
    INSULIN_LIKE_GROWTH_FACTOR_1("IGF-1", LabUnit.NANOMOLES_PER_LITER),
    IMMUNOGLOBIN_G("IgG", LabUnit.GRAMS_PER_LITER),
    IMMUNOGLOBIN_G4("IgG4", LabUnit.GRAMS_PER_LITER),
    IMMUNOGLOBIN_M("IgM", LabUnit.GRAMS_PER_LITER),
    IMMATURE_GRANULOCYTES_ABS("Immature granulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    IMMATURE_GRANULOCYTES_PCT("Immature granulocytes percentage", LabUnit.PERCENTAGE),
    INDIRECT_BILIRUBIN("Indirect bilirubin", LabUnit.MICROMOLES_PER_LITER),
    INSPIRED_OXYGEN_FRACTION("Fraction of inhaled oxygen", LabUnit.PERCENTAGE),
    INSULIN("Insulin", LabUnit.PICOMOLES_PER_LITER),
    INTERNATIONAL_NORMALIZED_RATIO("INR", LabUnit.NONE),
    IONIZED_CALCIUM_7_4("Ionized calcium (pH 7.4)", LabUnit.MILLIMOLES_PER_LITER),
    IONIZED_CALCIUM("Ionized calcium", LabUnit.MILLIMOLES_PER_LITER),
    IRON("Iron", LabUnit.MICROMOLES_PER_LITER),
    LACTATE_DEHYDROGENASE_LQ("LDH (liquor)", LabUnit.UNITS_PER_LITER),
    LACTATE_DEHYDROGENASE("LDH", LabUnit.UNITS_PER_LITER),
    LACTATE("Lactate", LabUnit.MILLIMOLES_PER_LITER),
    LDL_CHOLESTEROL("LDL", LabUnit.MILLIMOLES_PER_LITER),
    LEUKOCYTES_ABS("Leukocytes absolute", LabUnit.BILLIONS_PER_LITER),
    LEUKOCYTES_LQ("Leukocytes (liquor)", LabUnit.BILLIONS_PER_LITER),
    LIPASE("Lipase", LabUnit.UNITS_PER_LITER),
    LIPEMIC_INDEX("Lipemic index", LabUnit.NONE),
    LITHIUM_URINE("Lithium (urine)", LabUnit.MILLIMOLES_PER_LITER),
    LUTEINIZING_HORMONE("LH", LabUnit.UNITS_PER_LITER),
    LYMPHOCYTES_ABS("Lymphocytes absolute", LabUnit.BILLIONS_PER_LITER),
    LYMPHOCYTES_PCT("Lymphocytes percentage", LabUnit.PERCENTAGE),
    MAGNESIUM("Magnesium", LabUnit.MILLIMOLES_PER_LITER),
    MEAN_CORPUSCULAR_HEMOGLOBIN("MCH", LabUnit.FEMTOMOLES),
    MEAN_CORPUSCULAR_HEMOGLOBIN_CONCENTRATION("MCHC", LabUnit.MILLIMOLES_PER_LITER),
    MEAN_CORPUSCULAR_VOLUME("MCV", LabUnit.FEMTOLITERS),
    METANEPHRINE("Metanephrine ", LabUnit.NANOMOLES_PER_LITER),
    METHEMOGLOBIN_PCT("MetHb (%)", LabUnit.PERCENTAGE),
    METHEMOGLOBIN_F("MetHb", LabUnit.NONE),
    MONONUCLEAR_ABS("Mononuclear cell absolute", LabUnit.MILLIONS_PER_LITER),
    MYCOPHENYLATE("Mycophenylate", LabUnit.MILLIGRAMS_PER_LITER),
    MONOCYTES_ABS("Monocytes absolute", LabUnit.BILLIONS_PER_LITER),
    MONOCYTES_PCT("Monocytes percentage", LabUnit.PERCENTAGE),
    MEAN_PLATELET_VOLUME("MPV", LabUnit.FEMTOLITERS),
    MYELOCYTES_ABS("Myelocytes absolute", LabUnit.BILLIONS_PER_LITER),
    MYELOCYTES_PCT("Myelocytes percentage", LabUnit.PERCENTAGE),
    MYOGLOBIN("Myoglobin", LabUnit.NANOMOLES_PER_LITER),
    NEUTROPHILS_ABS("Neutrophil granulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    NEUTROPHILS_PCT("Neutrophil granulocytes percentage", LabUnit.PERCENTAGE),
    NK_PCT("Percentage NK-cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    NON_HDL_CHOLESTEROL("Non-HDL", LabUnit.NONE),
    NORMETANEPHRINE("Normetanephrine", LabUnit.NANOMOLES_PER_LITER),
    NEURON_SPECIFIC_ENOLASE("NSE", LabUnit.MICROGRAMS_PER_LITER),
    NT_PRO_BNP("NT-pro-BNP", LabUnit.PICOMOLES_PER_LITER),
    O2_CONTENT_BG("O2 content (blood gas)", LabUnit.MILLIMOLES_PER_LITER),
    O2_FRACTION_BG("O2 saturation (blood gas) (F)", LabUnit.NONE),
    O2_SATURATION_BG("O2 saturation (blood gas) (%)", LabUnit.PERCENTAGE),
    OSMOLALITY_URINE("Osmolality in urine", LabUnit.MILLI_OSMOLE_PER_KILOGRAM),
    OSMOLALITY("Osmolality", LabUnit.MILLI_OSMOLE_PER_KILOGRAM),
    P50_BG("P50 (blood gas)", LabUnit.KILO_PASCAL),
    PCO2_BG("pCO2 (blood gas)", LabUnit.KILO_PASCAL),
    COLLAGEN_EPINEPHRINE_CLOSURE_TIME("Col/Epi closure time", LabUnit.SECONDS),
    PH_BG("pH (blood gas)", LabUnit.NONE),
    PH_OV("pH OV", LabUnit.NONE),
    PH_URINE("pH (urine)", LabUnit.NONE),
    PHOSPHATE("Phosphate", LabUnit.MILLIMOLES_PER_LITER),
    PLASMA_PCT("Percentage plasma cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    POLYNUCLEAR_ABS("Polynuclear cells absolute", LabUnit.MILLIONS_PER_LITER),
    PO2_BG("pO2 (blood gas)", LabUnit.KILO_PASCAL),
    POTASSIUM_URINE("Potassium (urine)", LabUnit.MILLIMOLES_PER_LITER),
    POTASSIUM("Potassium", LabUnit.MILLIMOLES_PER_LITER),
    PREC_BCELL_PCT("% precursor-B-cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    PREC_MYL_PCT("% myeloid precursor-cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    PRNT50_SARS_COV_2("PRNT50 SARS-CoV-2", LabUnit.PRNT50),
    PROCALCITONIN("Procalcitonin", LabUnit.NANOGRAMS_PER_MILLILITER),
    PROGESTERONE_17_OH("17-OH Progesterone", LabUnit.NANOMOLES_PER_LITER),
    PROGESTERONE("Progesterone", LabUnit.NANOMOLES_PER_LITER),
    PROLACTIN("Prolactin", LabUnit.UNITS_PER_LITER),
    PROSTATE_SPECIFIC_ANTIGEN_RATIO("Ratio PSA", LabUnit.PERCENTAGE),
    PROSTATE_SPECIFIC_ANTIGEN("PSA", LabUnit.MICROGRAMS_PER_LITER),
    PROTHROMBIN_TIME("PT", LabUnit.SECONDS),
    PARTIAL_PROTHROMBIN_TIME("PPT", LabUnit.SECONDS),
    PARATHYROID_HORMONE_INTACT("PTH", LabUnit.PICOMOLES_PER_LITER),
    RED_BLOOD_CELL_DISTRIBUTION_WIDTH("RDW", LabUnit.PERCENTAGE),
    RETICULOCYTES_ABS("Reticulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    RETICULOCYTES_INDEX("Reticulocyte index", LabUnit.PERCENTAGE),
    RETICULOCYTES_PCT("Reticulocytes percentage", LabUnit.PERCENTAGE),
    REVERSE_TRIIODOTHYRONINE("Reverse T3", LabUnit.NANOMOLES_PER_LITER),
    ROD_GRANULOCYTES_ABS("Rod-shaped granulocytes absolute", LabUnit.BILLIONS_PER_LITER),
    ROD_GRANULOCYTES_PCT("Rod-shaped granulocytes percentage", LabUnit.PERCENTAGE),
    SEGMENTS_ABS("Segments absolute", LabUnit.BILLIONS_PER_LITER),
    SEGMENTS_PCT("Segments percentage", LabUnit.PERCENTAGE),
    SEROTONIN("Serotonin", LabUnit.MICROMOLES_PER_LITER),
    SEX_HORMONE_BINDING_GLOBULIN("SHBG", LabUnit.NANOMOLES_PER_LITER),
    SMIGK_SMIGL_RATIO("SmIgK/SmIgL ratio", LabUnit.NONE),
    SODIUM_URINE("Sodium (urine)", LabUnit.MILLIMOLES_PER_LITER),
    SODIUM("Sodium", LabUnit.MILLIMOLES_PER_LITER),
    SOLUBLE_IL2R("Soluble IL-2R", LabUnit.UNITS_PER_MILLILITER),
    SPECIFIC_WEIGHT_URINE("Specific weight urine", LabUnit.KILOGRAMS_PER_LITER),
    SQUAMOUS_CELL_CARCINOMA_ANTIGEN("SCC antigen", LabUnit.MICROGRAMS_PER_LITER),
    SSA_AB("SS-A antibodies", LabUnit.UNITS_PER_MILLILITER),
    SSB_AB("SS-B antibodies", LabUnit.UNITS_PER_MILLILITER),
    TACROLIMUS("Tacrolimus", LabUnit.MICROGRAMS_PER_LITER),
    TCELL_ABS("T-Cells absolute", LabUnit.MILLIONS_PER_LITER),
    TCELL_PCT("Percentage T-cells", LabUnit.PERCENTAGE_OF_LEUKOCYTES),
    THROMBO_CITRATE("Thrombo citrate", LabUnit.BILLIONS_PER_LITER),
    TESTOSTERONE("Testosterone", LabUnit.NANOMOLES_PER_LITER),
    THYROGLOBULIN_AB("hs-Thyreoglobulin antibody", LabUnit.UNITS_PER_MILLILITER),
    THYROGLOBULIN_RECOVERY("hs-Thyreoglobulin recovery", LabUnit.PERCENTAGE),
    THYROGLOBULIN("hs-Thyreoglobulin", LabUnit.MICROGRAMS_PER_LITER),
    THREE_MT("3-MT", LabUnit.NANOMOLES_PER_LITER),
    THROMBOCYTES_ABS("Absolute thrombocyte count", LabUnit.BILLIONS_PER_LITER),
    THYROID_STIMULATING_HORMONE("TSH", LabUnit.MILLIUNITS_PER_LITER),
    TOTAL_BILIRUBIN("Total bilirubin", LabUnit.MICROMOLES_PER_LITER),
    TOTAL_PROTEIN_LQ("Total protein in liquor", LabUnit.GRAMS_PER_LITER),
    TOTAL_PROTEIN_URINE_CREATININE_RATIO_24H("Total protein urine to creatinine ratio (24h urine)", LabUnit.MILLIGRAMS_PER_MILLIMOLE),
    TOTAL_PROTEIN_URINE_CREATININE_RATIO("Total protein urine to creatinine ratio", LabUnit.MILLIGRAMS_PER_MILLIMOLE),
    TOTAL_PROTEIN_URINE("Total protein (urine)", LabUnit.GRAMS_PER_LITER),
    TOTAL_PROTEIN_24U("Total protein (24h urine)", LabUnit.GRAMS_PER_DAY),
    TOTAL_PROTEIN("Total protein", LabUnit.GRAMS_PER_LITER),
    TOTAL_TRIIODOTHYRONINE("Total T3", LabUnit.PICOMOLES_PER_LITER),
    TOTAL_THYROXINE("Total T4", LabUnit.NANOMOLES_PER_LITER),
    TRANSFERRIN("Transferrin", LabUnit.GRAMS_PER_LITER),
    TRIGLYCERIDE("Triglyceride", LabUnit.MILLIMOLES_PER_LITER),
    HIGH_SENSITIVITY_TROPONIN_T("HsTnT", LabUnit.NANOGRAMS_PER_LITER),
    TRANSFERRIN_SATURATION("TSAT", LabUnit.PERCENTAGE),
    UREA_URINE("Urea in urine", LabUnit.MILLIMOLES_PER_LITER),
    UREA("Urea", LabUnit.MILLIMOLES_PER_LITER),
    URIC_ACID("Uric acid", LabUnit.MILLIMOLES_PER_LITER),
    URINE_SPECIFIC_GRAVITY("Urine specific gravity", LabUnit.NONE),
    URINE_VOLUME_24H_ENDO("24-hour volume urine for endocrinology", LabUnit.MILLILITERS),
    URINE_VOLUME_24H_SUB("24-hour volume urine for solid ", LabUnit.MILLILITERS),
    URINE_VOLUME_24H("24-hour volume urine", LabUnit.MILLILITERS),
    UROBILINOGEN_URINE("Urobilinogen (urine)", LabUnit.MICROMOLES_PER_LITER),
    VITAMIN_A("Vitamin A", LabUnit.MICROMOLES_PER_LITER),
    VITAMIN_B12("Vitamin B12", LabUnit.PICOMOLES_PER_LITER),
    VITAMIN_D_1_25_DI_OH("1.25-di-OH Vitamin D", LabUnit.PICOMOLES_PER_LITER),
    VITAMIN_D_25_OH("Vitamin D 25-OH", LabUnit.NANOMOLES_PER_LITER),
    VITAMIN_E("Vitamin E blood", LabUnit.MICROMOLES_PER_LITER),
    VITAMIN_K("Vitamin K", LabUnit.NANOMOLES_PER_LITER),
    VON_WILLEBRAND_FACTOR_ACTIVITY("vWF activity", LabUnit.UNITS_PER_MILLILITER),
    VON_WILLEBRAND_FACTOR_ANTIGEN("vWF antigen", LabUnit.UNITS_PER_MILLILITER),
    VON_WILLEBRAND_FACTOR_COLLAGEN_BINDING("vWF collagen binding", LabUnit.UNITS_PER_MILLILITER),
    WHITE_BLOOD_CELL_COUNT("WBCC", LabUnit.MILLIONS_PER_MILLILITER),
    WHITE_BLOOD_CELL_RED_BLOOD_CELL_RATIO_LQ("White blood cell / Red blood cell ratio LQ", LabUnit.PERCENTAGE),
    ZINC("Zinc in whole blood", LabUnit.MICROMOLES_PER_LITER),
    UNKNOWN("Unknown", LabUnit.NONE);

    override fun display(): String {
        return display
    }
}
