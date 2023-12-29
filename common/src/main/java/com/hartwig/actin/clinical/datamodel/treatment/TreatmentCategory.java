package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.HashSet;
import java.util.Set;

import com.hartwig.actin.Displayable;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public enum TreatmentCategory implements Displayable {
    CHEMOTHERAPY,
    RADIOTHERAPY,
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

    public static final Set<TreatmentCategory> cancerTreatmentSet =
            new HashSet<>(Set.of(CHEMOTHERAPY, TARGETED_THERAPY, IMMUNOTHERAPY, HORMONE_THERAPY, TRIAL, CAR_T, TCR_T, GENE_THERAPY));
}
