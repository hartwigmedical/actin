package com.hartwig.actin.molecular.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularTreatmentEvidence {

    @NotNull
    public abstract String genomicEvent();

    @NotNull
    public abstract String treatment();

    public abstract boolean onLabel();

    @NotNull
    public abstract EvidenceLevel level();

    @NotNull
    public abstract EvidenceDirection direction();

}
