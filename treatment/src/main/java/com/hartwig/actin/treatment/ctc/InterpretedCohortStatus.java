package com.hartwig.actin.treatment.ctc;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class InterpretedCohortStatus {

    public abstract boolean open();

    public abstract boolean slotsAvailable();
}
