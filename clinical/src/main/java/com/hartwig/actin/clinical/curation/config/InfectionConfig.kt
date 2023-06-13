package com.hartwig.actin.clinical.curation.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class InfectionConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Override
    public boolean ignore() {
        return false;
    }

    @NotNull
    public abstract String interpretation();
}
