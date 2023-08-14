package com.hartwig.actin.clinical.datamodel.treatment;

import com.hartwig.actin.Displayable;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public enum TreatmentCategory implements Displayable {
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
    CAR_T,
    TCR_T,
    GENE_THERAPY,
    PROPHYLACTIC_TREATMENT,
    ABLATION;

    @Override
    @NotNull
    public String display() {
        return TreatmentCategoryResolver.toString(this).toLowerCase();
    }
}
