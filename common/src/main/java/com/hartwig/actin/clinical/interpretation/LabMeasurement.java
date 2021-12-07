package com.hartwig.actin.clinical.interpretation;

import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ALAT("ALAT"),
    ALBUMIN("ALB"),
    ALP("ALP"),
    ASAT("ASAT"),
    CREATININE("CREA"),
    CREATININE_CLEARANCE_CG("CGCRCL"),
    EGFR_CKD_EPI("CKD-EPIeGFR"),
    EGFR_MDRD("MDRDeGFR"),
    HEMOGLOBIN("Hb"),
    LDH("LDH"),
    LEUKOCYTES_ABS("LEUKO-ABS"),
    NEUTROPHILS_ABS("NEUTRO-ABS"),
    NEUTROPHILS_ABS_EDA("NEUTRO-ABS-eDA"),
    THROMBOCYTES_ABS("THROMBO-ABS"),
    TOTAL_BILIRUBIN("TBIL");

    @NotNull
    private final String code;

    LabMeasurement(@NotNull final String code) {
        this.code = code;
    }

    @NotNull
    public String code() {
        return code;
    }
}
