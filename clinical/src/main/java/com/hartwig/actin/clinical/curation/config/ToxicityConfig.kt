package com.hartwig.actin.clinical.curation.config;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ToxicityConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Override
    public abstract boolean ignore();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> categories();

    @Nullable
    public abstract Integer grade();
}
