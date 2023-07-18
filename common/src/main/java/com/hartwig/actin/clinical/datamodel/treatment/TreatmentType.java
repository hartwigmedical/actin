package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;

public interface TreatmentType {

    @NotNull
    String display();

    @NotNull
    TreatmentCategory category();
}
