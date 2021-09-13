package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ClinicalStatus {

    public abstract int who();

    public abstract boolean hasCurrentInfection();

    @Nullable
    public abstract String infectionDescription();

    public abstract boolean hasSigAberrationLatestEcg();

    @Nullable
    public abstract String ecgAberrationDescription();

    @NotNull
    public abstract String cancerRelatedComplication();
}
