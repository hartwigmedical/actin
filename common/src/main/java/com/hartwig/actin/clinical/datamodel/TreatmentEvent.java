package com.hartwig.actin.clinical.datamodel;

import java.time.Year;
import java.time.YearMonth;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentEvent {

    @NotNull
    public abstract Set<Treatment> treatments();

    @Nullable
    public abstract Year startYear();

    @Nullable
    public abstract YearMonth startMonth();

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
    public abstract Intent intent();
}
