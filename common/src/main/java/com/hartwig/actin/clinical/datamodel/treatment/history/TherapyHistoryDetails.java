package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.clinical.datamodel.LocationCategory;
import com.hartwig.actin.clinical.datamodel.ObservedToxicity;

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
    public abstract LocalDate ongoingAsOf();

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
