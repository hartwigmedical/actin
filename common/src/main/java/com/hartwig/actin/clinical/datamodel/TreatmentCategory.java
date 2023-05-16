package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public enum TreatmentCategory {
    CHEMOTHERAPY(true),
    RADIOTHERAPY(true),
    CHEMORADIOTHERAPY(false),
    TARGETED_THERAPY(true),
    IMMUNOTHERAPY(true),
    HORMONE_THERAPY(true),
    ANTIVIRAL_THERAPY(false),
    SUPPORTIVE_TREATMENT(true),
    SURGERY(false),
    TRANSPLANTATION(true),
    TRIAL(true),
    CAR_T(true),
    TCR_T(false),
    GENE_THERAPY(false),
    PROPHYLACTIC_TREATMENT(false),
    THERMAL_THERAPY(false);

    private final boolean hasType;

    TreatmentCategory(final boolean hasType) {
        this.hasType = hasType;
    }

    @NotNull
    public String display() {
        return TreatmentCategoryResolver.toString(this).toLowerCase();
    }

    public boolean hasType() {
        return hasType;
    }
}
