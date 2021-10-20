package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Toxicity {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract LocalDate evaluatedDate();

    @NotNull
    public abstract ToxicitySource source();

    @Nullable
    public abstract Integer grade();

}
