package com.hartwig.actin.clinical.curation.config;

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LesionLocationConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    public boolean ignore() {
        return false;
    }

    @NotNull
    public abstract String location();

    @Nullable
    public abstract LesionLocationCategory category();
}
