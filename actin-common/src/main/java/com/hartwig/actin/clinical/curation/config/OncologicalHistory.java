package com.hartwig.actin.clinical.curation.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OncologicalHistory {

    @NotNull
    public abstract String input();

    public abstract boolean ignore();

    @Nullable
    public abstract Object curatedObject();

}
