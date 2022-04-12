package com.hartwig.actin.molecular.orange.datamodel.virus;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class VirusInterpreterEntry {

    // TODO

    @NotNull
    public abstract String name();
}
