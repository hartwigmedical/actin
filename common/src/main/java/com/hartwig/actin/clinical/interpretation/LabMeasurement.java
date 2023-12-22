package com.hartwig.actin.clinical.interpretation;

import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME("APTT", "APTT", LabUnit.SECONDS),
    AMYLASE("AMYL", "amylase", LabUnit.UNITS_PER_LITER),
    ALANINE_AMINOTRANSFERASE("ALAT", "ALAT", LabUnit.UNITS_PER_LITER),
    ALBUMIN("ALB", "albumin", LabUnit.GRAMS_PER_LITER),
    ALPHA_FETOPROTEIN("AFP", "alpha fetoprotein", LabUnit.MICROGRAMS_PER_LITER),
    ALKALINE_PHOSPHATASE("ALP", "alkaline phosphatase", LabUnit.UNITS_PER_LITER),
    ASPARTATE_AMINOTRANSFERASE("ASAT", "ASAT", LabUnit.UNITS_PER_LITER),
    CARBOHYDRATE_ANTIGEN_125("C125", "CA 125", LabUnit.KILOUNITS_PER_LITER),
    CARBOHYDRATE_ANTIGEN_19_9("C199", "CA 19-9", LabUnit.KILOUNITS_PER_LITER),
    CARBOHYDRATE_ANTIGEN_15_3("C153", "CA 15-3", LabUnit.KILOUNITS_PER_LITER),
    CALCIUM("Ca", "calcium", LabUnit.MILLIMOLES_PER_LITER),
    CARCINOEMBRYONIC_ANTIGEN("CEA", "CEA", LabUnit.MICROGRAMS_PER_LITER),
    CORRECTED_CALCIUM("Ca_C", "corrected calcium", LabUnit.MILLIMOLES_PER_LITER),
    CREATININE("CREA", "creatinine", LabUnit.MICROMOLES_PER_LITER),
    CREATININE_CLEARANCE_CG("CGCRCL", "creatinine clearance (Cockcroft-Gault)", LabUnit.NONE),
    DDIMER("DDIM", "D-dimer", LabUnit.MILLIGRAMS_PER_LITER),
    DIRECT_BILIRUBIN("DBIL", "direct bilirubin", LabUnit.MICROMOLES_PER_LITER),
    EGFR_CKD_EPI("CKD-EPIeGFR", "estimated GFR (CKD-EPI)", LabUnit.MILLILITERS_PER_MINUTE),
    EGFR_MDRD("MDRDeGFR", "estimated GFR (MDRD)", LabUnit.NONE),
    FREE_T4("FT4", "free T4", LabUnit.PICOMOLES_PER_LITER),
    HCG_AND_BETA_HCG("HCG", "HCG", LabUnit.INTERNATIONAL_UNITS_PER_LITER),
    HEMOGLOBIN("Hb", "hemoglobin", LabUnit.MILLIMOLES_PER_LITER),
    INTERNATIONAL_NORMALIZED_RATIO("INR", "INR", LabUnit.NONE),
    INTERNATIONAL_NORMALIZED_RATIO_POCT("POCT_INR", "POCT INR", LabUnit.UNITS_OF_INR),
    IONIZED_CALCIUM("B_ICA", "ionized calcium", LabUnit.MILLIMOLES_PER_LITER),
    LACTATE_DEHYDROGENASE("LDH", "LDH", LabUnit.UNITS_PER_LITER),
    LEUKOCYTES_ABS("LEUKO-ABS", "absolute leukocyte count", LabUnit.BILLIONS_PER_LITER),
    LIPASE("LIPA", "lipase", LabUnit.UNITS_PER_LITER),
    LYMPHOCYTES_ABS_EDA("LYMPHO-ABS-eDA", "absolute lymphocyte count eDA", LabUnit.BILLIONS_PER_LITER),
    LYMPHOCYTES_ABS_EDM("LYMPHO-ABS-eDM", "absolute lymphocyte count eDM", LabUnit.BILLIONS_PER_LITER),
    MAGNESIUM("MG", "magnesium", LabUnit.MILLIMOLES_PER_LITER),
    NEUTROPHILS_ABS("NEUTRO-ABS", "absolute neutrophil count", LabUnit.BILLIONS_PER_LITER),
    NEUTROPHILS_ABS_EDA("NEUTRO-ABS-eDA", "absolute neutrophil count eDA", LabUnit.BILLIONS_PER_LITER),
    NT_PRO_BNP("BNP", "NT-proBNP", LabUnit.PICOMOLES_PER_LITER),
    PSA("PSA", "PSA", LabUnit.MICROGRAMS_PER_LITER),
    PHOSPHORUS("P", "phosphorus", LabUnit.MILLIMOLES_PER_LITER),
    POTASSIUM("K", "potassium", LabUnit.MILLIMOLES_PER_LITER),
    PROTHROMBIN_TIME("PT", "PT", LabUnit.SECONDS),
    PROTHROMBIN_TIME_POCT("POCT_PT", "POCT PT", LabUnit.SECONDS),
    THROMBOCYTES_ABS("THROMBO-ABS", "absolute thrombocyte count", LabUnit.BILLIONS_PER_LITER),
    THROMBOCYTES_ABS_M("THROMBO-ABS-M", "absolute thrombocyte count M", LabUnit.BILLIONS_PER_LITER),
    TOTAL_BILIRUBIN("TBIL", "total bilirubin", LabUnit.MICROMOLES_PER_LITER),
    TOTAL_PROTEIN_URINE("TE_U", "total urine protein", LabUnit.GRAMS_PER_LITER),
    TRIGLYCERIDE("TG", "triglycerides", LabUnit.MILLIMOLES_PER_LITER),
    TROPONIN_I_T("HSTNT", "troponin I and T", LabUnit.NANOGRAMS_PER_LITER);

    @NotNull
    private final String code;
    @NotNull
    private final String display;
    @NotNull
    private final LabUnit defaultUnit;

    LabMeasurement(@NotNull final String code, @NotNull final String display, @NotNull final LabUnit defaultUnit) {
        this.code = code;
        this.display = display;
        this.defaultUnit = defaultUnit;
    }

    @NotNull
    public String code() {
        return code;
    }

    @NotNull
    public String display() {
        return display;
    }

    @NotNull
    public LabUnit defaultUnit() {
        return defaultUnit;
    }
}
