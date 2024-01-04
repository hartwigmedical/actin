package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ExternalTrial {

    @NotNull
    public abstract String title();

    @NotNull
    public abstract Set<String> countries();

    @NotNull
    public abstract String url();

    @NotNull
    public abstract String nctId();
}
