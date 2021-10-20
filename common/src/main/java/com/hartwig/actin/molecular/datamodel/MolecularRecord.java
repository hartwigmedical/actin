package com.hartwig.actin.molecular.datamodel;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

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

    @NotNull
    @Value.Derived
    public Set<String> actionableGenomicEvents() {
        Set<String> events = Sets.newTreeSet();
        for (GenomicTreatmentEvidence evidence : genomicTreatmentEvidences()) {
            events.add(evidence.genomicEvent());
        }
        return events;
    }
}
