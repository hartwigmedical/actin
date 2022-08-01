package com.hartwig.actin.doid.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Edge {

    @NotNull
    public abstract String subject();

    @NotNull
    public abstract String subjectDoid();

    @NotNull
    public abstract String object();

    @NotNull
    public abstract String objectDoid();

    @NotNull
    public abstract String predicate();
}
