package com.hartwig.actin.treatment.input.single;

import com.hartwig.actin.treatment.input.datamodel.TreatmentCategoryInput;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneTreatmentCategoryOrTypeOneInteger {

    @NotNull
    public abstract TreatmentCategoryInput treatment();

    public abstract int integer();
}