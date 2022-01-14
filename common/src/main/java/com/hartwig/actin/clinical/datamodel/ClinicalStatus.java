package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ClinicalStatus {

    @Nullable
    public abstract Integer who();

    @Nullable
    public abstract InfectionStatus infectionStatus();

    @Nullable
    public abstract ECG ecg();

    @Nullable
    public abstract Double lvef();
}
