package com.hartwig.actin.datamodel.molecular;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularRecord {

    @NotNull
    public abstract String sampleId();

    public abstract boolean hasReliableQuality();

    public abstract boolean hasReliablePurity();

    @NotNull
    public abstract List<GenomicTreatmentEvidence> genomicTreatmentEvidences();
}
