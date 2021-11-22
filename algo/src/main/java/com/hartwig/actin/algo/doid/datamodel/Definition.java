package com.hartwig.actin.algo.doid.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class Definition {

    @NotNull
    public abstract String definitionVal();

    @NotNull
    public abstract List<String> definitionXrefs();
}
