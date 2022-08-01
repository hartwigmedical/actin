package com.hartwig.actin.doid.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class AdenoSquamousMapping {

    @NotNull
    public abstract String adenoSquamousDoid();

    @NotNull
    public abstract String squamousDoid();

    @NotNull
    public abstract String adenoDoid();
}
