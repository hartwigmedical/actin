package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OrangeRecord {

    @NotNull
    public abstract String sampleId();

    @Nullable
    public abstract LocalDate date();

    public abstract boolean hasReliableQuality();

    @NotNull
    public abstract String microsatelliteStabilityStatus();

    @NotNull
    public abstract String homologousRepairStatus();

    public abstract double tumorMutationalBurden();

    public abstract int tumorMutationalLoad();

    @NotNull
    public abstract List<TreatmentEvidence> evidences();

}
