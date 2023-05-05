package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Toxicity {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> categories();

    @Nullable
    public abstract Integer grade();

    @Nullable
    public abstract ToxicityType toxicityType();
}
