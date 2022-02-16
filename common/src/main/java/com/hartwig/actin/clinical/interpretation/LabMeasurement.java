package com.hartwig.actin.clinical.interpretation;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ALANINE_AMINOTRANSFERASE("ALAT", "U/l"),
    ASPARTATE_AMINOTRANSFERASE("ASAT", "U/l"),
    ALKALINE_PHOSPHATASE("ALP", "U/l"),
    ALPHA_FETOPROTEIN("AFP", "ug/L"),
    LACTATE_DEHYDROGENASE("LDH", "U/L"),
    INTERNATIONAL_NORMALIZED_RATIO("INR", ""),
    INTERNATIONAL_NORMALIZED_RATIO_POCT("POCT_INR","INR"),
    ALBUMIN("ALB", "g/L"),
    CA_125("C125", "kU/l"),
    HCG_AND_BETA_HCG("HCG", "IU/L"),
    CREATININE("CREA", "umol/l"),
    CREATININE_CLEARANCE_CG("CGCRCL", Strings.EMPTY),
    EGFR_CKD_EPI("CKD-EPIeGFR", "mL/min"),
    EGFR_MDRD("MDRDeGFR", Strings.EMPTY),
    LEUKOCYTES_ABS("LEUKO-ABS", "10^9/L"),
    NEUTROPHILS_ABS("NEUTRO-ABS", "10^9/L"),
    NEUTROPHILS_ABS_EDA("NEUTRO-ABS-eDA", "10*9/L"),
    HEMOGLOBIN("Hb", "mmol/L"),
    THROMBOCYTES_ABS("THROMBO-ABS", "10*9/L"),
    LYMPHOCYTES_ABS_EDA("LYMPHO-ABS-eDA", "10*9/L"),
    LYMPHOCYTES_ABS_EDM("LYMPHO-ABS-eDM", "10*9/L"),
    PROTHROMBIN_TIME("PT", "sec"),
    PROTHROMBIN_TIME_POCT("POCT_PT","sec"),
    ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME("APTT", "sec"),
    DIRECT_BILIRUBIN("DBIL", "umol/l"),
    TOTAL_BILIRUBIN("TBIL", "umol/l"),
    CALCIUM("Ca", "mmol/l"),
    IONIZED_CALCIUM("B_ICA", "mmol/l"),
    CORRECTED_CALCIUM("Ca_C", "mmol/L"),
    NT_PRO_BNP("BNP", "pmol/l"),
    TROPONIN_IT("HSTNT", "ng/L"),
    TRIGLYCERIDE("TG", "mmol/l"),
    POTASSIUM("K", "mmol/l"),
    MAGNESIUM("MG", "mmol/l"),
    PHOSPHORUS("P", "mmol/l"),
    TOTAL_PROTEIN_URINE("TE_U", "g/l");

    @NotNull
    private final String code;
    @NotNull
    private final String expectedUnit;

    LabMeasurement(@NotNull final String code, @NotNull final String expectedUnit) {
        this.code = code;
        this.expectedUnit = expectedUnit;
    }

    @NotNull
    public String code() {
        return code;
    }

    @NotNull
    public String expectedUnit() {
        return expectedUnit;
    }
}
