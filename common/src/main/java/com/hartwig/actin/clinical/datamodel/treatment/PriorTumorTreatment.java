package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorTumorTreatment {

    @NotNull
    public abstract String name();

    @Nullable
    public abstract Integer startYear();

    @Nullable
    public abstract Integer startMonth();

    @Nullable
    public abstract Integer stopYear();

    @Nullable
    public abstract Integer stopMonth();

    @Nullable
    public abstract Integer cycles();

    @Nullable
    public abstract String bestResponse();

    @Nullable
    public abstract String stopReason();

    @NotNull
    public abstract Set<TreatmentCategory> categories();

    public abstract boolean isSystemic();

    @Nullable
    public abstract String chemoType();

    @Nullable
    public abstract String immunoType();

    @Nullable
    public abstract String targetedType();

    @Nullable
    public abstract String hormoneType();

    @Nullable
    public abstract String radioType();

    @Nullable
    public abstract String carTType();

    @Nullable
    public abstract String transplantType();

    @Nullable
    public abstract String supportiveType();

    @Nullable
    public abstract String trialAcronym();

    @Nullable
    public abstract String ablationType();
}
