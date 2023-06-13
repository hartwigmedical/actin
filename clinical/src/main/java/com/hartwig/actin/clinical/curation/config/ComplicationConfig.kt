package com.hartwig.actin.clinical.curation.config;

import com.hartwig.actin.clinical.datamodel.Complication;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ComplicationConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Override
    public abstract boolean ignore();

    public abstract boolean impliesUnknownComplicationState();

    @Nullable
    public abstract Complication curated();

}
