package com.hartwig.actin.molecular.datamodel.evidence;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EvidenceEntry {

    @NotNull
    public abstract String event();

    @NotNull
    public abstract String sourceEvent();

    @NotNull
    public abstract EvidenceType sourceType();

    @NotNull
    public abstract String treatment();
}
