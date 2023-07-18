package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;

public enum RadiotherapyType implements TreatmentType {
    BRACHYTHERAPY,
    CYBERKNIFE,
    RADIOISOTOPE,
    STEREOTACTIC;

    @NotNull
    public TreatmentCategory category() {
        return TreatmentCategory.RADIOTHERAPY;
    }

    @NotNull
    @Override
    public String display() {
        return toString().replace("_", " ").toLowerCase();
    }
}
