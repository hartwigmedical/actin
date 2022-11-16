package com.hartwig.actin.molecular.orange.evidence.filter;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ApplicabilityFilterKey {

    @NotNull
    public abstract String gene();

//    @NotNull
//    public abstract EvidenceLevel level();

    @NotNull
    public abstract String treatment();
}
