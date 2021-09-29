package com.hartwig.actin.clinical.curation.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ToxicityConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @NotNull
    public abstract String standardizedTerm();

    public abstract int grade();
}
