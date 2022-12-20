package com.hartwig.actin.molecular.orange.datamodel.chord;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ChordRecord {

    // TODO Change to enum

    @NotNull
    public abstract String hrStatus();
}
