package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;
import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Medication {

    @NotNull
    public abstract String name();

    @Nullable
    public abstract MedicationStatus status();

    @Nullable
    public abstract String administrationRoute();

    @NotNull
    public abstract Dosage dosage();

    @Nullable
    public abstract LocalDate startDate();

    @Nullable
    public abstract LocalDate stopDate();

    @NotNull
    public abstract List<CypInteraction> cypInteractions();

    public abstract QTProlongatingRisk qtProlongatingRisk();

    @Nullable
    public abstract AtcClassification atc();
}
