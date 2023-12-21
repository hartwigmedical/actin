package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ECG {

    public abstract boolean hasSigAberrationLatestECG();

    @Nullable
    public abstract String aberrationDescription();

    @Nullable
    public abstract ECGMeasure qtcfMeasure();

    @Nullable
    public abstract ECGMeasure jtcMeasure();
}
