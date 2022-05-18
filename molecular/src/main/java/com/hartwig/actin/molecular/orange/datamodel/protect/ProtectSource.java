package com.hartwig.actin.molecular.orange.datamodel.protect;

import com.hartwig.actin.molecular.datamodel.evidence.EvidenceType;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ProtectSource {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String event();

    @NotNull
    public abstract EvidenceType type();

    @Nullable
    public abstract Integer rangeRank();

}
