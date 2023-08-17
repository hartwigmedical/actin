package com.hartwig.actin.treatment.input.single;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneSpecificTreatmentOneInteger {

    public abstract int integer();

    @NotNull
    public abstract Treatment treatment();
}
