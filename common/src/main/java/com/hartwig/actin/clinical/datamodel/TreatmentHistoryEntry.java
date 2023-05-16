package com.hartwig.actin.clinical.datamodel;

import java.time.Month;
import java.time.Year;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentHistoryEntry {

    @NotNull
    public abstract Set<Treatment> treatments();

    @Nullable
    public abstract Year startYear();

    @Nullable
    public abstract Month startMonth();

    @Nullable
    public abstract Intent intent();

    @Nullable
    public abstract Boolean isTrial();

    @Nullable
    public abstract String trialAcronym();

    @Nullable
    public abstract TherapyHistoryDetails therapyHistoryDetails();

    @Nullable
    public abstract SurgeryHistoryDetails surgeryHistoryDetails();
}
