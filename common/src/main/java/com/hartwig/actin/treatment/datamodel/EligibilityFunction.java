package com.hartwig.actin.treatment.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EligibilityFunction {

    @NotNull
    public abstract EligibilityRule rule();

    @NotNull
    public abstract Set<Object> parameters();
}
