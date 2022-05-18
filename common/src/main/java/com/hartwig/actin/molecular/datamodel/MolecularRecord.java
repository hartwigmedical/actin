package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.interpretation.MappedActinEvents;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularRecord {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract ExperimentType type();

    @Nullable
    public abstract LocalDate date();

    public abstract boolean hasReliableQuality();

    @NotNull
    public abstract MolecularCharacteristics characteristics();

    @NotNull
    public abstract MolecularDrivers drivers();

    @NotNull
    public abstract Set<PharmacoEntry> pharmaco();

    @NotNull
    public abstract MolecularEvidence evidence();

    // TODO Remove mappedEvents. Actin should be able to resolve inclusion based on evidence.
    @NotNull
    public abstract MappedActinEvents mappedEvents();

}
