package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorOtherCondition {

    @NotNull
    public abstract String name();

    @Nullable
    public abstract Integer year();

    @Nullable
    public abstract Integer month();

    @NotNull
    public abstract Set<String> doids();

    @NotNull
    public abstract String category();

    public abstract boolean isContraindicationForTherapy();

}
