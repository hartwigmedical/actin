package com.hartwig.actin.datamodel.clinical;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class BloodTransfusion {

    @NotNull
    public abstract LocalDate date();

    @NotNull
    public abstract String product();
}
