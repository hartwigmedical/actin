package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TherapyHistoryDetails {

    @Nullable
    public abstract Integer stopYear();

    @Nullable
    public abstract Integer stopMonth();

    @Nullable
    public abstract Boolean ongoing();

    @Nullable
    public abstract Integer cycles();

    @Nullable
    public abstract TreatmentResponse bestResponse();

    @Nullable
    public abstract StopReason stopReason();

    @Nullable
    public abstract String stopReasonDetail();

    @Nullable
    public abstract Set<ObservedToxicity> toxicities();

    @Nullable
    public abstract Set<LocationCategory> locationCategories();

    @Nullable
    public abstract Set<String> locations();
}
