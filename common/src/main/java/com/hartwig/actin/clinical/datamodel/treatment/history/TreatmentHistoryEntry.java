package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentHistoryEntry {

    @NotNull
    public abstract Set<Treatment> treatments();

    @Nullable
    public abstract Integer startYear();

    @Nullable
    public abstract Integer startMonth();

    @Nullable
    public abstract Set<Intent> intents();

    @Nullable
    public abstract Boolean isTrial();

    @Nullable
    public abstract String trialAcronym();

    @Nullable
    public abstract TherapyHistoryDetails therapyHistoryDetails();

}
