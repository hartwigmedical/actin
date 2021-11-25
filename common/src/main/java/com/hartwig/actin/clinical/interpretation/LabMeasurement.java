package com.hartwig.actin.clinical.interpretation;

import org.jetbrains.annotations.NotNull;

public enum LabMeasurement {
    ALAT("ALAT"),
    ALBUMIN("ALB"),
    ALP("ALP"),
    ASAT("ASAT"),
    CDK_EPI_EGFR("CDK-EPIeGFR"),
    CREATININE("CREA"),
    HEMOGLOBIN("Hb"),
    LDH("LDH"),
    THROMBOCYTES("THROMBO"),
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
