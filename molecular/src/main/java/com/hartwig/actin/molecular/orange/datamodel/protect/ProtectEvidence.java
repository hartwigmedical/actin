package com.hartwig.actin.molecular.orange.datamodel.protect;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ProtectEvidence {

    public abstract boolean reported();

    @Nullable
    public abstract String gene();

    @NotNull
    public abstract String event();

    @Nullable
    public abstract Boolean eventIsHighDriver();

    @NotNull
    public abstract String treatment();

    public abstract boolean onLabel();

    @NotNull
    public abstract EvidenceLevel level();

    @NotNull
    public abstract EvidenceDirection direction();

    @NotNull
    public abstract Set<ProtectSource> sources();

}
