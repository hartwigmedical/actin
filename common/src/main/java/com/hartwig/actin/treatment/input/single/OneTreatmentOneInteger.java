package com.hartwig.actin.treatment.input.single;

import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneTreatmentOneInteger {

    @NotNull
    public abstract TreatmentInput treatment();

    public abstract int integer();
}