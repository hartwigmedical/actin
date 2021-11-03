package com.hartwig.actin.treatment.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Eligibility {

    @NotNull
    public abstract String reference();

    @NotNull
    public abstract String description();

    @NotNull
    public abstract EligibilityFunction function();

}
