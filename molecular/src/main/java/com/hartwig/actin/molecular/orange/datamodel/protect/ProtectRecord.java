package com.hartwig.actin.molecular.orange.datamodel.protect;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ProtectRecord {

    @NotNull
    public abstract Set<ProtectEvidence> evidences();
}
