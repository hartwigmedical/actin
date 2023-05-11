package com.hartwig.actin.clinical.datamodel;

import java.time.Year;
import java.time.YearMonth;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TherapyHistoryEntry implements TreatmentHistoryEntry {

    @Nullable
    public abstract Year endYear();

    @Nullable
    public abstract YearMonth endMonth();

    @Nullable
    public abstract Boolean ongoing();

    @Nullable
    public abstract Integer chemoCycles();

    @Nullable
    public abstract TreatmentResponse bestResponse();

    @Nullable
    public abstract StopReason stopReason();

    @Nullable
    public abstract String stopReasonDetail();

    @Nullable
    public abstract Set<Toxicity> toxicities();

    @Nullable
    public abstract Set<LocationCategory> locationCategories();

    @Nullable
    public abstract Set<String> locations();
}
