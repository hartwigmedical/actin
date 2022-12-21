package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleGainLoss {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract PurpleGainLossInterpretation interpretation();

    public abstract int minCopies();

    public abstract int maxCopies();
}
