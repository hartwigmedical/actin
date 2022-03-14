package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public enum TreatmentCategory {
    CHEMOTHERAPY,
    RADIOTHERAPY,
    CHEMORADIOTHERAPY,
    TARGETED_THERAPY,
    IMMUNOTHERAPY,
    HORMONE_THERAPY,
    ANTIVIRAL_THERAPY,
    SUPPORTIVE_TREATMENT,
    SURGERY,
    TRANSPLANTATION,
    TRIAL,
    VACCINE,
    CAR_T,
    TCR_T,
    GENE_THERAPY;

    @NotNull
    public String display() {
        return TreatmentCategoryResolver.toString(this).toLowerCase();
    }
}
