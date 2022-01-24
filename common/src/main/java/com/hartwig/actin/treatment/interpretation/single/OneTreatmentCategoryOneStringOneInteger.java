package com.hartwig.actin.treatment.interpretation.single;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneTreatmentCategoryOneStringOneInteger {

    @NotNull
    public abstract TreatmentCategory treatmentCategory();

    @NotNull
    public abstract String string();

    public abstract int integer();
}
