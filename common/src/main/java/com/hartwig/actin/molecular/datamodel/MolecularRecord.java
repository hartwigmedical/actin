package com.hartwig.actin.molecular.datamodel;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularRecord {

    @NotNull
    public abstract String sampleId();

    public abstract boolean hasReliableQuality();

    @NotNull
    public abstract Set<String> configuredPrimaryTumorDoids();

    @NotNull
    public abstract List<MolecularTreatmentEvidence> evidences();
}
