package com.hartwig.actin.datamodel.clinical;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorSecondPrimary {

    @NotNull
    public abstract String tumorLocation();

    @NotNull
    public abstract String tumorSubLocation();

    @NotNull
    public abstract String tumorType();

    @NotNull
    public abstract String tumorSubType();

    @NotNull
    public abstract Set<String> doids();

    @Nullable
    public abstract Integer diagnosedYear();

    public abstract boolean isSecondPrimaryActive();

}