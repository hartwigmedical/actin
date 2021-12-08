package com.hartwig.actin.clinical.interpretation;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ALAT("ALAT", "U/l"),
    ALBUMIN("ALB", "g/L"),
    ALP("ALP", "U/l"),
    APTT("APTT", "sec"),
    ASAT("ASAT", "U/l"),
    CREATININE("CREA", "umol/l"),
    CREATININE_CLEARANCE_CG("CGCRCL", Strings.EMPTY),
    DIRECT_BILIRUBIN("DBIL", "umol/l"),
    EGFR_CKD_EPI("CKD-EPIeGFR", "mL/min"),
    EGFR_MDRD("MDRDeGFR", Strings.EMPTY),
    HEMOGLOBIN("Hb", "mmol/L"),
    INR("INR", ""),
    LDH("LDH", "U/L"),
    LEUKOCYTES_ABS("LEUKO-ABS", "10^9/L"),
    NEUTROPHILS_ABS("NEUTRO-ABS", "10^9/L"),
    NEUTROPHILS_ABS_EDA("NEUTRO-ABS-eDA", "10*9/L"),
    PT("PT", "sec"),
    THROMBOCYTES_ABS("THROMBO-ABS", "10*9/L"),
    TOTAL_BILIRUBIN("TBIL", "umol/l");

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
