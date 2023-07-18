package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;

public enum OtherTreatmentType implements TreatmentType {
    ALLOGENIC(TreatmentCategory.TRANSPLANTATION),
    AUTOLOGOUS(TreatmentCategory.TRANSPLANTATION),
    MICROWAVE(TreatmentCategory.ABLATION),
    RADIOFREQUENCY(TreatmentCategory.ABLATION),
    HYPERTHERMIA(TreatmentCategory.ABLATION);

    @NotNull
    private final TreatmentCategory category;

    OtherTreatmentType(@NotNull TreatmentCategory category) {
        this.category = category;
    }

    @NotNull
    public TreatmentCategory category() {
        return category;
    }

    @NotNull
    @Override
    public String display() {
        return toString().replace("_", " ").toLowerCase();
    }
}
