package com.hartwig.actin.doid.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Definition {

    @NotNull
    public abstract String val();

    @NotNull
    public abstract List<String> xrefs();
}
