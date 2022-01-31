package com.hartwig.actin.molecular.orange.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentEvidence {

    public abstract boolean reported();

    @Nullable
    public abstract String gene();

    @NotNull
    public abstract String event();

    @Nullable
    public abstract Integer rangeRank();

    @NotNull
    public abstract String treatment();

    public abstract boolean onLabel();

    @NotNull
    public abstract EvidenceType type();

    @NotNull
    public abstract EvidenceLevel level();

    @NotNull
    public abstract EvidenceDirection direction();

    @NotNull
    public abstract Set<String> sources();

}
