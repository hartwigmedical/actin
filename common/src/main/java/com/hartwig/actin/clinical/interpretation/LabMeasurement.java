package com.hartwig.actin.clinical.interpretation;

import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME("APTT", LabUnit.SECONDS),
    AMYLASE("AMYL", LabUnit.UNITS_PER_LITER),
    ALANINE_AMINOTRANSFERASE("ALAT", LabUnit.UNITS_PER_LITER),
    ALBUMIN("ALB", LabUnit.GRAMS_PER_LITER),
    ALPHA_FETOPROTEIN("AFP", LabUnit.MICROGRAMS_PER_LITER),
    ALKALINE_PHOSPHATASE("ALP", LabUnit.UNITS_PER_LITER),
    ASPARTATE_AMINOTRANSFERASE("ASAT", LabUnit.UNITS_PER_LITER),
    CA_125("C125", LabUnit.KILOUNITS_PER_LITER),
    CA_199("C199", LabUnit.KILOUNITS_PER_LITER),
    CA_153("C153", LabUnit.KILOUNITS_PER_LITER),
    CALCIUM("Ca", LabUnit.MILLIMOLES_PER_LITER),
    CEA("CEA", LabUnit.MICROGRAMS_PER_LITER),
    CORRECTED_CALCIUM("Ca_C", LabUnit.MILLIMOLES_PER_LITER),
    CREATININE("Creatinine", LabUnit.MICROMOLES_PER_LITER),
    CREATININE_CLEARANCE_CG("CGCRCL", LabUnit.NONE),
    DDIMER("DDIM", LabUnit.MILLIGRAMS_PER_LITER),
    DIRECT_BILIRUBIN("DBIL", LabUnit.MICROMOLES_PER_LITER),
    EGFR_CKD_EPI("CKD-EPIeGFR", LabUnit.MILLILITERS_PER_MINUTE),
    EGFR_MDRD("MDRDeGFR", LabUnit.NONE),
    FT4("FT4", LabUnit.PICOMOLES_PER_LITER),
    HCG_AND_BETA_HCG("HCG", LabUnit.INTERNATIONAL_UNITS_PER_LITER),
    HEMOGLOBIN("Hb", LabUnit.MILLIMOLES_PER_LITER),
    INTERNATIONAL_NORMALIZED_RATIO("INR", LabUnit.NONE),
    INTERNATIONAL_NORMALIZED_RATIO_POCT("POCT_INR", LabUnit.UNITS_OF_INR),
    IONIZED_CALCIUM("B_ICA", LabUnit.MILLIMOLES_PER_LITER),
    LACTATE_DEHYDROGENASE("LDH", LabUnit.UNITS_PER_LITER),
    LEUKOCYTES_ABS("LEUKO-ABS", LabUnit.BILLIONS_PER_LITER),
    LIPASE("LIPA", LabUnit.UNITS_PER_LITER),
    LYMPHOCYTES_ABS_EDA("LYMPHO-ABS-eDA", LabUnit.BILLIONS_PER_LITER),
    LYMPHOCYTES_ABS_EDM("LYMPHO-ABS-eDM", LabUnit.BILLIONS_PER_LITER),
    MAGNESIUM("MG", LabUnit.MILLIMOLES_PER_LITER),
    NEUTROPHILS_ABS("NEUTRO-ABS", LabUnit.BILLIONS_PER_LITER),
    NEUTROPHILS_ABS_EDA("NEUTRO-ABS-eDA", LabUnit.BILLIONS_PER_LITER),
    NT_PRO_BNP("BNP", LabUnit.PICOMOLES_PER_LITER),
    PSA("PSA", LabUnit.MICROGRAMS_PER_LITER),
    PHOSPHORUS("P", LabUnit.MILLIMOLES_PER_LITER),
    POTASSIUM("K", LabUnit.MILLIMOLES_PER_LITER),
    PROTHROMBIN_TIME("PT", LabUnit.SECONDS),
    PROTHROMBIN_TIME_POCT("POCT_PT", LabUnit.SECONDS),
    THROMBOCYTES_ABS("THROMBO-ABS", LabUnit.BILLIONS_PER_LITER),
    THROMBOCYTES_ABS_M("THROMBO-ABS-M", LabUnit.BILLIONS_PER_LITER),
    TOTAL_BILIRUBIN("TBIL", LabUnit.MICROMOLES_PER_LITER),
    TOTAL_PROTEIN_URINE("TE_U", LabUnit.GRAMS_PER_LITER),
    TRIGLYCERIDE("TG", LabUnit.MILLIMOLES_PER_LITER),
    TROPONIN_IT("HSTNT", LabUnit.NANOGRAMS_PER_LITER);

    @NotNull
    private final String code;
    @NotNull
    private final LabUnit defaultUnit;

    LabMeasurement(@NotNull final String code, @NotNull final LabUnit defaultUnit) {
        this.code = code;
        this.defaultUnit = defaultUnit;
    }

    @NotNull
    public String code() {
        return code;
    }

    @NotNull
    public LabUnit defaultUnit() {
        return defaultUnit;
    }
}
