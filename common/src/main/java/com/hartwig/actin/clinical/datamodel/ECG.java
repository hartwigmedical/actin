package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ECG {

    public abstract boolean hasSigAberrationLatestECG();

    @NotNull
    public abstract String aberrationDescription();

    @Nullable
    public abstract Integer qtcfValue();

    @Nullable
    public abstract String qtcfUnit();
}
