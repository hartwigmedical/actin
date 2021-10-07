package com.hartwig.actin.clinical.curation.config;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PrimaryTumorConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @NotNull
    public abstract String primaryTumorLocation();

    @NotNull
    public abstract String primaryTumorSubLocation();

    @NotNull
    public abstract String primaryTumorType();

    @NotNull
    public abstract String primaryTumorSubType();

    @NotNull
    public abstract String primaryTumorExtraDetails();

    @NotNull
    public abstract Set<String> doids();
}
