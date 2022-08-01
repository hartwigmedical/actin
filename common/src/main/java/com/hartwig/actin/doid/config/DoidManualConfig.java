package com.hartwig.actin.doid.config;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class DoidManualConfig {

    @NotNull
    public abstract Set<String> mainCancerDoids();

    @NotNull
    public abstract Set<AdenoSquamousMapping> adenoSquamousMappings();
}
