package com.hartwig.actin.clinical.datamodel.treatment;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public interface TreatmentType extends Displayable {

    @NotNull
    TreatmentCategory category();
}
