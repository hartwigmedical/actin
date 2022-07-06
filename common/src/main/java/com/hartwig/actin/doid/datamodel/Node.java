package com.hartwig.actin.doid.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class Node {

    @NotNull
    public abstract String doid();

    @NotNull
    public abstract String url();

    @Nullable
    public abstract String term();

    @Nullable
    public abstract String type();

    @Nullable
    public abstract Metadata metadata();

    @Nullable
    @Value.Derived
    public String snomedConceptId() {
        return metadata() != null ? metadata().snomedConceptId() : null;
    }
}
