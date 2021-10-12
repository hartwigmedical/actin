package com.hartwig.actin.datamodel.clinical;

import java.time.LocalDate;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Complication {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> doids();

    @NotNull
    public abstract String specialty();

    @NotNull
    public abstract LocalDate onsetDate();

    @NotNull
    public abstract String category();

    @NotNull
    public abstract String status();
}
